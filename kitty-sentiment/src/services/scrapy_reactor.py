"""
在同一进程内用 Scrapy CrawlerRunner + Twisted reactor 调度爬虫（队列串行执行）。

说明：Twisted 在同一进程只能安全地运行一次 reactor，因此多次爬取通过队列串行；
这比反复 subprocess 启进程更符合 Scrapy 的用法。

调试：爬虫在名为「scrapy-reactor」的线程里跑，断点需在该线程或勾选 IDE「暂停所有线程」。
日志：见项目 logs/scrapy.log；控制台会输出（LOG_STDOUT）；本模块用 loguru 打 [Scrapy] 前缀便于区分。
"""

from __future__ import annotations

import logging
import os
import threading
from datetime import datetime
from typing import Any, Callable, List, Optional, Tuple

from loguru import logger

# kitty-sentiment 项目根（含 scrapy.cfg）
_PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

_lock = threading.Lock()
_queue: List[
    Tuple[str, Optional[type], dict[str, Any], dict[str, Any], dict[str, Any]]
] = []
_reactor_thread: Optional[threading.Thread] = None
_reactor_ready = threading.Event()
_process_next_ref: Optional[Callable[[], None]] = None
_scrapy_logging_configured = False
# 仅在 reactor 线程赋值，供主线程 callFromThread，避免在 Flask 线程 import reactor
_reactor_ref: Any = None


def _update_task_status(task_id: int, status: str) -> None:
    """爬虫结束后回写任务状态（用于手动触发任务）。"""
    try:
        from src.main import app
        from src.models import Task

        db = app.session_factory()
        try:
            task = db.query(Task).filter(Task.id == task_id).first()
            if not task:
                return
            task.status = status
            db.commit()
            logger.info("[Scrapy] 任务状态已回写 task_id={} status={}", task_id, status)
        finally:
            db.close()
    except Exception:
        logger.exception("[Scrapy] 回写任务状态失败 task_id={} status={}", task_id, status)


def _update_task_execution(
    execution_id: int,
    *,
    status: Optional[str] = None,
    spider_name: Optional[str] = None,
    items_count: Optional[int] = None,
    request_count: Optional[int] = None,
    error_message: Optional[str] = None,
    error_trace: Optional[str] = None,
    finished: bool = False,
) -> None:
    """回写任务执行历史。"""
    try:
        from src.main import app
        from src.models import TaskExecution

        db = app.session_factory()
        try:
            row = db.query(TaskExecution).filter(TaskExecution.id == execution_id).first()
            if not row:
                return
            if status is not None:
                row.status = status
            if spider_name is not None:
                row.spider_name = spider_name
            if items_count is not None:
                row.items_count = items_count
            if request_count is not None:
                row.request_count = request_count
            if error_message is not None:
                row.error_message = error_message
            if error_trace is not None:
                row.error_trace = error_trace
            if finished:
                row.finished_at = datetime.now()
            db.commit()
        finally:
            db.close()
    except Exception:
        logger.exception("[Scrapy] 回写任务执行历史失败 execution_id={}", execution_id)


def _setup_scrapy_logging(settings: Any) -> None:
    """
    非 `scrapy crawl` CLI 启动时不会自动配置 logging，必须显式 configure_logging，
    否则 scrapy/spider 日志几乎不可见。

    仅有 LOG_FILE 时根 handler 只会写文件；这里再给 root 加一个 StreamHandler，
    便于在运行 Flask 的终端里直接看到 Scrapy 输出。
    """
    global _scrapy_logging_configured
    if _scrapy_logging_configured:
        return
    import sys

    from scrapy.utils.log import configure_logging

    configure_logging(settings=settings, install_root_handler=True)

    try:
        from src.config.settings import get_settings

        debug = bool(get_settings().debug)
    except Exception:
        debug = os.environ.get("APP_DEBUG", "").lower() in ("1", "true", "yes")

    level = logging.DEBUG if debug else logging.INFO
    if os.environ.get("SCRAPY_LOG_LEVEL"):
        lvl = os.environ["SCRAPY_LOG_LEVEL"].upper()
        if lvl == "DEBUG":
            level = logging.DEBUG
        elif lvl == "INFO":
            level = logging.INFO
        elif lvl == "WARNING":
            level = logging.WARNING

    for name in (
            "scrapy",
            "scrapy.core.engine",
            "scrapy.core.scraper",
            "scrapy.middleware",
            "scrapy.downloadermiddlewares",
            "scrapy.spiders",
    ):
        logging.getLogger(name).setLevel(level)
    logging.getLogger("twisted").setLevel(logging.INFO)

    # 文件已由 install_scrapy_root_handler 处理；补充终端输出
    fmt = settings.get("LOG_FORMAT") or "%(asctime)s [%(name)s] %(levelname)s: %(message)s"
    datefmt = settings.get("LOG_DATEFORMAT")
    has_stderr = any(
        isinstance(h, logging.StreamHandler)
        and getattr(h, "stream", None) in (sys.stderr, sys.__stderr__)
        for h in logging.root.handlers
    )
    if not has_stderr:
        sh = logging.StreamHandler(sys.stderr)
        sh.setLevel(level)
        sh.setFormatter(logging.Formatter(fmt=fmt, datefmt=datefmt))
        logging.root.addHandler(sh)

    _scrapy_logging_configured = True
    logger.info(
        "[Scrapy] 日志已初始化（configure_logging + stderr），级别={}，文件={}",
        logging.getLevelName(level),
        settings.get("LOG_FILE"),
    )


def _reactor_main() -> None:
    global _process_next_ref, _reactor_ref

    import asyncio
    import sys

    if sys.platform == "win32":
        try:
            asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())
        except Exception:
            pass

    # AsyncioSelectorReactor 依赖当前线程的 asyncio loop；必须在 reactor 线程内创建
    _loop = asyncio.new_event_loop()
    asyncio.set_event_loop(_loop)
    from twisted.internet import asyncioreactor

    asyncioreactor.install(_loop)

    from twisted.internet import reactor

    _reactor_ref = reactor

    os.chdir(_PROJECT_ROOT)
    os.environ.setdefault("SCRAPY_SETTINGS_MODULE", "src.scrapy_settings")
    from twisted.python.failure import Failure
    from scrapy import signals
    from scrapy.crawler import CrawlerRunner
    from scrapy.spiderloader import SpiderLoader
    from scrapy.utils.project import get_project_settings

    os.makedirs(os.path.join(_PROJECT_ROOT, "logs"), exist_ok=True)

    busy = False

    def process_next() -> None:
        nonlocal busy
        if busy:
            return
        with _lock:
            if not _queue:
                return
            spider_name, spider_cls, spider_kwargs, runtime_settings, execution_context = _queue.pop(0)
        settings = get_project_settings()
        for key, value in runtime_settings.items():
            settings.set(key, value, priority="cmdline")
        _setup_scrapy_logging(settings)
        runner = CrawlerRunner(settings)
        loader = SpiderLoader.from_settings(settings)
        busy = True
        try:
            if spider_cls is None:
                spider_cls = loader.load(spider_name)
        except Exception:
            logger.exception("[Scrapy] 加载爬虫失败: {}", spider_name)
            execution_id = execution_context.get("task_execution_id")
            if isinstance(execution_id, int):
                _update_task_execution(
                    execution_id,
                    status="failed",
                    spider_name=spider_name,
                    error_message=f"加载爬虫失败: {spider_name}",
                    finished=True,
                )
            task_id = execution_context.get("task_id")
            trigger_mode = execution_context.get("trigger_mode")
            if trigger_mode == "manual" and isinstance(task_id, int):
                _update_task_status(task_id, "failed")
            busy = False
            reactor.callLater(0, process_next)
            return

        logger.info(
            "[Scrapy] 开始 crawl spider={} kwargs={} runtime={}",
            spider_name,
            spider_kwargs,
            {
                "ROBOTSTXT_OBEY": runtime_settings.get("ROBOTSTXT_OBEY"),
                "COOKIES_ENABLED": runtime_settings.get("COOKIES_ENABLED"),
            },
        )
        execution_id = execution_context.get("task_execution_id")
        if isinstance(execution_id, int):
            _update_task_execution(execution_id, status="running", spider_name=spider_name)
        crawler = runner.create_crawler(spider_cls)
        spider_errors: List[str] = []

        def _on_spider_error(failure: Any, _response: Any, _spider: Any) -> None:
            try:
                spider_errors.append(failure.getErrorMessage())
            except Exception:
                spider_errors.append(str(failure))

        crawler.signals.connect(_on_spider_error, signal=signals.spider_error)
        deferred = runner.crawl(crawler, **spider_kwargs)

        def _done(_result: Any) -> Any:
            nonlocal busy
            busy = False
            stats = crawler.stats.get_stats() if crawler.stats else {}
            item_count = int(stats.get("item_scraped_count", 0) or 0)
            req_count = int(stats.get("downloader/request_count", 0) or 0)
            robots_forbidden = int(
                stats.get(
                    "downloader/exception_type_count/scrapy.exceptions.IgnoreRequest",
                    0,
                )
                or 0
            )

            if isinstance(_result, Failure):
                err = _result.getErrorMessage()
                if "Forbidden by robots.txt" in err:
                    logger.warning(
                        "[Scrapy] crawl 被 robots.txt 拒绝 spider={} err={}",
                        spider_name,
                        err,
                    )
                else:
                    logger.error(
                        "[Scrapy] crawl 结束（失败）spider={} err={}",
                        spider_name,
                        err,
                    )
                final_status = "failed"
                final_error = err
            else:
                if robots_forbidden > 0 and item_count == 0:
                    logger.warning(
                        "[Scrapy] crawl 完成但被 robots 拒绝 spider={} req={} forbidden={}",
                        spider_name,
                        req_count,
                        robots_forbidden,
                    )
                    final_status = "failed"
                    final_error = "Forbidden by robots.txt"
                elif spider_errors:
                    logger.warning(
                        "[Scrapy] crawl 完成但有 spider_error spider={} errors={}",
                        spider_name,
                        spider_errors[:3],
                    )
                    final_status = "failed"
                    final_error = "; ".join(spider_errors[:3])
                else:
                    logger.info(
                        "[Scrapy] crawl 结束（成功）spider={} req={} items={}",
                        spider_name,
                        req_count,
                        item_count,
                    )
                    final_status = "completed"
                    final_error = None

            task_id = execution_context.get("task_id")
            trigger_mode = execution_context.get("trigger_mode")
            if trigger_mode == "manual" and isinstance(task_id, int):
                _update_task_status(task_id, final_status)
            execution_id = execution_context.get("task_execution_id")
            if isinstance(execution_id, int):
                _update_task_execution(
                    execution_id,
                    status=final_status,
                    spider_name=spider_name,
                    items_count=item_count,
                    request_count=req_count,
                    error_message=final_error,
                    error_trace=str(_result) if isinstance(_result, Failure) else None,
                    finished=True,
                )
            reactor.callLater(0, process_next)
            return _result

        deferred.addBoth(_done)

    _process_next_ref = process_next

    def _on_start() -> None:
        _reactor_ready.set()
        logger.info("[Scrapy] Twisted reactor 已运行，线程={}", threading.current_thread().name)
        process_next()

    reactor.callWhenRunning(_on_start)
    reactor.run(installSignalHandlers=False)


def ensure_reactor_thread() -> None:
    """确保 Twisted reactor 在守护线程中运行。"""
    global _reactor_thread
    with _lock:
        if _reactor_thread is not None and _reactor_thread.is_alive():
            return
        _reactor_ready.clear()
        _reactor_thread = threading.Thread(
            target=_reactor_main,
            daemon=True,
            name="scrapy-reactor",
        )
        _reactor_thread.start()
    if not _reactor_ready.wait(timeout=60):
        logger.error("Scrapy reactor 线程启动超时")


def schedule_spider(
    spider_name: str,
    spider_cls: Optional[type] = None,
    spider_kwargs: Optional[dict[str, Any]] = None,
    runtime_settings: Optional[dict[str, Any]] = None,
    execution_context: Optional[dict[str, Any]] = None,
) -> None:
    """
    将爬虫加入队列，由 CrawlerRunner 在 reactor 线程中执行。
    若 reactor 未启动则先启动。
    """
    kwargs = dict(spider_kwargs or {})
    settings = dict(runtime_settings or {})
    context = dict(execution_context or {})
    logger.info(
        "[Scrapy] 入队 spider={} kwargs={} runtime={}",
        spider_name,
        kwargs,
        {
            "ROBOTSTXT_OBEY": settings.get("ROBOTSTXT_OBEY"),
            "COOKIES_ENABLED": settings.get("COOKIES_ENABLED"),
        },
    )
    ensure_reactor_thread()
    with _lock:
        _queue.append((spider_name, spider_cls, kwargs, settings, context))

    def _kick() -> None:
        fn = _process_next_ref
        if fn is None:
            logger.warning("[Scrapy] _kick 时调度函数未就绪")
            return
        fn()

    r = _reactor_ref
    if r is None:
        logger.error("[Scrapy] reactor 引用未就绪，无法 callFromThread")
        return
    r.callFromThread(_kick)
