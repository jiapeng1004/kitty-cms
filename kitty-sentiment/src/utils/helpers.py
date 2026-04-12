"""
工具函数
"""

from loguru import logger
from datetime import datetime
from typing import Optional


def setup_logger(level: str = "INFO"):
    """配置日志"""
    logger.remove()
    logger.add(
        "logs/kitty_sentiment_{time:YYYY-MM-DD}.log",
        rotation="1 day",
        retention="7 days",
        level=level,
        format="{time:YYYY-MM-DD HH:mm:ss} | {level: <8} | {name}:{function}:{line} - {message}"
    )
    return logger


def sanitize_text(text: str) -> str:
    """清理文本"""
    if not text:
        return ""
    
    # 移除多余空白
    text = " ".join(text.split())
    
    # 移除控制字符
    text = "".join(char for char in text if char.isprintable())
    
    return text


def parse_date(date_str: str) -> Optional[datetime]:
    """解析日期字符串"""
    if not date_str:
        return None
    
    formats = [
        "%Y-%m-%d %H:%M:%S",
        "%Y-%m-%d",
        "%Y/%m/%d %H:%M:%S",
        "%Y/%m/%d",
        "%d-%m-%Y %H:%M:%S",
        "%d/%m/%Y %H:%M:%S",
    ]
    
    for fmt in formats:
        try:
            return datetime.strptime(date_str, fmt)
        except ValueError:
            continue
    
    return None
