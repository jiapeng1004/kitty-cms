"""信息源名称 / ID 到 Scrapy 爬虫名的解析（未配置 spider_name 时的回退逻辑）。"""

from __future__ import annotations

from typing import Optional

_NAME_HINTS: tuple[tuple[str, str], ...] = (
    ("bilibili", "bilibili_hot"),
    ("哔哩", "bilibili_hot"),
    ("b站", "bilibili_hot"),
    ("tophub", "today_hot"),
    ("今日热榜", "today_hot"),
    ("今日", "today_hot"),
    ("weibo", "weibo_hot"),
    ("微博", "weibo_hot"),
    ("zhihu", "zhihu_hot"),
    ("知乎", "zhihu_hot"),
)

_BY_ID: dict[int, str] = {
    1: "bilibili_hot",
    2: "today_hot",
}


def resolve_spider_name(source_name: str, source_id: Optional[int] = None) -> Optional[str]:
    name = (source_name or "").strip().lower()
    for hint, spider in _NAME_HINTS:
        if hint in name:
            return spider
    if source_id is not None and source_id in _BY_ID:
        return _BY_ID[source_id]
    return None
