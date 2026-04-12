"""爬取频率：Unix 风格 5 段 Cron，或关键字 manual（仅手动触发，不参与调度）。"""

from __future__ import annotations

from datetime import datetime

from croniter import CroniterBadCronError, croniter


def normalize_task_frequency(raw: str) -> str:
    """
    校验并规范化 frequency 字段。
    - ``manual``：不自动调度，仅手动启动任务。
    - 否则须为 5 段 cron：分 时 日 月 周（与 croniter 一致）。
    """
    if raw is None:
        raise ValueError("爬取频率不能为空")

    s = str(raw).strip()
    if not s:
        raise ValueError("爬取频率不能为空")

    lower = s.lower()
    if lower == "manual":
        return "manual"

    parts = s.split()
    if len(parts) != 5:
        raise ValueError(
            "须为 5 段 Cron（分 时 日 月 周），例如：0 */6 * * * 表示每 6 小时整点执行"
        )

    try:
        croniter(s, datetime.now())
    except CroniterBadCronError as e:
        raise ValueError(f"Cron 表达式无效: {e}") from e
    except Exception as e:
        raise ValueError(f"Cron 表达式无效: {e}") from e

    return s
