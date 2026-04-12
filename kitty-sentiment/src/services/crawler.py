"""
爬虫服务：通过 Scrapy CrawlerRunner（见 scrapy_reactor）在同一进程内调度。
运行参数在任务启动时动态生成（任务级/信息源级配置），而不是写死在 scrapy_settings.py。
支持从数据库配置中动态加载 Spider 实现（source.config.spiderCode）。
"""

import json
import os
from typing import Any, Optional, Tuple

from loguru import logger

from src.config.settings import settings as app_settings
from src.schemas import CrawlResponse
from src.services.scrapy_reactor import schedule_spider
from src.utils.spider_code import compile_spider_code, parse_source_config


class CrawlerService:
    """爬虫服务类"""

    def __init__(self):
        self.cwd = os.path.dirname(
            os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        )

    def _load_source_config(self, source: Any) -> dict[str, Any]:
        return parse_source_config(getattr(source, "config", None))

    def _resolve_spider_spec(self, source: Any) -> Tuple[Optional[str], Optional[type], dict[str, Any]]:
        """返回 (spider_name, spider_cls, source_config)。Spider 名唯一来源：sources.name。"""
        cfg = self._load_source_config(source)
        display_name = (getattr(source, "name", None) or "").strip()
        impl_mode = str(cfg.get("implementation") or cfg.get("implMode") or "").strip().lower()
        if not impl_mode:
            impl_mode = "db" if (cfg.get("spiderCode") or cfg.get("spider_code")) else "local"

        if impl_mode == "local":
            if not display_name:
                logger.error("source(id={}) 名称为空，无法加载本地 Spider", getattr(source, "id", None))
                return None, None, cfg
            return display_name, None, cfg

        code = cfg.get("spiderCode") or cfg.get("spider_code")
        if not code:
            logger.error("source(id={}) 缺少 config.spiderCode", getattr(source, "id", None))
            return None, None, cfg

        try:
            spider_cls, _warnings = compile_spider_code(
                code,
                spider_class=cfg.get("spiderClass") or cfg.get("spider_class"),
                spider_name=display_name or None,
            )
        except Exception:
            logger.exception("动态 Spider 代码执行失败")
            return None, None, cfg

        spider_name = getattr(spider_cls, "name")
        return spider_name, spider_cls, cfg

    def _normalize_runtime_config(self, runtime_config: Any) -> dict[str, Any]:
        if runtime_config is None:
            return {}
        if isinstance(runtime_config, dict):
            return runtime_config
        if isinstance(runtime_config, str):
            s = runtime_config.strip()
            if not s:
                return {}
            try:
                obj = json.loads(s)
                return obj if isinstance(obj, dict) else {}
            except json.JSONDecodeError:
                return {}
        return {}

    def _build_runtime_settings(self, spider_name: str, runtime_config: Any) -> dict[str, Any]:
        """把任务/信息源配置映射成 Scrapy 运行时参数。"""
        cfg = self._normalize_runtime_config(runtime_config)
        logs_dir = os.path.join(self.cwd, "logs")
        os.makedirs(logs_dir, exist_ok=True)

        runtime: dict[str, Any] = {
            "USER_AGENT": app_settings.crawler.user_agent,
            "DOWNLOAD_DELAY": app_settings.crawler.delay,
            "DOWNLOAD_TIMEOUT": app_settings.crawler.timeout,
            "RETRY_TIMES": app_settings.crawler.max_retries,
            "CONCURRENT_REQUESTS": 8,
            "ROBOTSTXT_OBEY": True,
            "COOKIES_ENABLED": False,
            "ITEM_PIPELINES": {"src.pipelines.SentimentPipeline": 300},
            "DOWNLOADER_MIDDLEWARES": {
                "scrapy.downloadermiddlewares.useragent.UserAgentMiddleware": None,
                "scrapy.downloadermiddlewares.robotstxt.RobotsTxtMiddleware": 100,
            },
            "EXTENSIONS": {"scrapy.extensions.telnet.TelnetConsole": None},
            "AUTOTHROTTLE_ENABLED": True,
            "AUTOTHROTTLE_START_DELAY": 5,
            "AUTOTHROTTLE_MAX_DELAY": 60,
            "AUTOTHROTTLE_TARGET_CONCURRENCY": 1.0,
            "MEMUSAGE_ENABLED": True,
            "MEMUSAGE_LIMIT_MB": 512,
            "LOG_LEVEL": os.environ.get("SCRAPY_LOG_LEVEL", "INFO"),
            "LOG_FILE": os.path.join(logs_dir, f"{spider_name}.log"),
            "LOG_ENABLED": True,
            "LOG_STDOUT": False,
        }

        mapping = {
            "robotstxtObey": "ROBOTSTXT_OBEY",
            "obeyRobots": "ROBOTSTXT_OBEY",
            "cookiesEnabled": "COOKIES_ENABLED",
            "userAgent": "USER_AGENT",
            "downloadDelay": "DOWNLOAD_DELAY",
            "concurrentRequests": "CONCURRENT_REQUESTS",
            "downloadTimeout": "DOWNLOAD_TIMEOUT",
            "maxRetries": "RETRY_TIMES",
            "logLevel": "LOG_LEVEL",
        }
        bool_keys = {"ROBOTSTXT_OBEY", "COOKIES_ENABLED"}

        def _to_bool(v: Any) -> bool:
            if isinstance(v, bool):
                return v
            if isinstance(v, str):
                return v.strip().lower() in {"1", "true", "yes", "on"}
            return bool(v)

        for key, value in cfg.items():
            target = mapping.get(key, key if key.isupper() else None)
            if target is None:
                continue
            if target in bool_keys:
                value = _to_bool(value)
            runtime[target] = value
        return runtime

    def run_spider(
        self,
        spider_name: str,
        runtime_config: Any = None,
        spider_cls: Optional[type] = None,
        execution_context: Optional[dict[str, Any]] = None,
        **kwargs,
    ) -> dict:
        """
        将爬虫加入 Scrapy 队列，由 CrawlerRunner 在 Twisted reactor 线程中执行。
        立即返回（不等待爬取结束）。
        """
        try:
            logger.info(f"排队启动爬虫: {spider_name} kwargs={kwargs}")
            spider_kwargs = dict(kwargs)
            runtime_settings = self._build_runtime_settings(spider_name, runtime_config)
            schedule_spider(
                spider_name,
                spider_cls=spider_cls,
                spider_kwargs=spider_kwargs,
                runtime_settings=runtime_settings,
                execution_context=execution_context or {},
            )
            return {
                "status": "queued",
                "message": "已加入 Scrapy 执行队列（进程内 CrawlerRunner）",
            }
        except Exception as e:
            logger.exception(f"排队爬虫失败: {spider_name}")
            return {
                "status": "error",
                "message": f"爬虫排队失败: {str(e)}",
            }

    def crawl_source(
        self,
        source: Any,
        max_pages: int = 10,
        runtime_config: Any = None,
        task_context: Optional[dict[str, Any]] = None,
    ) -> CrawlResponse:
        """按信息源执行 Scrapy 爬虫。"""
        spider, spider_cls, source_cfg = self._resolve_spider_spec(source)
        if not spider:
            return CrawlResponse(
                success=False,
                message="无法解析爬虫：请配置本地 spiderName 或 DB spiderCode",
                errors=["支持 local（src/spiders）与 db（source.config.spiderCode）双实现"],
            )
        merged_cfg = dict(source_cfg)
        merged_cfg.update(self._normalize_runtime_config(runtime_config))

        spider_kwargs: dict[str, Any] = {
            "max_pages": max_pages,
        }
        if task_context:
            # 注入到 spider 实例上下文：Spider.__init__(**kwargs) 会挂载为实例属性
            if "task_id" in task_context:
                spider_kwargs["task_id"] = task_context["task_id"]
                spider_kwargs["taskId"] = task_context["task_id"]
            if "task_type" in task_context:
                spider_kwargs["task_type"] = task_context["task_type"]
                spider_kwargs["taskType"] = task_context["task_type"]
            if "trigger_mode" in task_context:
                spider_kwargs["trigger_mode"] = task_context["trigger_mode"]
            spider_kwargs["source_id"] = getattr(source, "id", None)
            spider_kwargs["source_name"] = getattr(source, "name", None)

        out = self.run_spider(
            spider,
            spider_cls=spider_cls,
            runtime_config=merged_cfg,
            execution_context=task_context,
            **spider_kwargs,
        )
        ok = out.get("status") == "queued"
        err = out.get("message", "") if not ok else ""
        return CrawlResponse(
            success=ok,
            message=out.get("message", ""),
            crawled_count=0,
            new_items=0,
            errors=[] if ok else [err],
        )

    def crawl_single_url(self, url: str) -> dict:
        """单 URL 抓取（占位：复杂站点请走信息源爬虫）。"""
        if not url or not url.startswith(("http://", "https://")):
            return {"success": False, "error": "无效的 URL"}
        return {
            "success": False,
            "error": "单链接抓取未实现，请使用「信息源」触发对应爬虫",
        }

    def run_all_spiders(self) -> dict:
        """完全 DB 化模式下不再支持“按硬编码名称批量启动”。"""
        return {
            "status": "error",
            "message": "已启用 DB 化 Spider；请按 source.config.spiderCode 逐个 source 启动",
        }

    def get_spider_status(self, spider_name: str) -> dict:
        """获取爬虫状态（基于日志文件，仅供参考）。"""
        log_file = os.path.join(self.cwd, "logs", f"{spider_name}.log")
        if os.path.exists(log_file):
            with open(log_file, "r", encoding="utf-8") as f:
                lines = f.readlines()
                last_lines = lines[-10:] if len(lines) > 10 else lines

            return {
                "status": "running" if len(last_lines) > 0 else "idle",
                "last_logs": last_lines,
            }
        return {
            "status": "idle",
            "message": "未找到爬虫日志",
        }
