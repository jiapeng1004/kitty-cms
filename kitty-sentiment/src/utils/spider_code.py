"""
Spider 代码（DB 存储）校验与加载。

约定：
- 不需要在 spiderCode 里写 import，运行时会注入常用依赖。
- 必须定义 scrapy.Spider 子类。
"""

from __future__ import annotations

import ast
import json
import re
from datetime import datetime, timedelta
from typing import Any, Optional, Tuple

import pydantic
import scrapy
from pydantic import BaseModel, Field, ValidationError

from src.items import SentimentItem


def parse_source_config(raw: Any) -> dict[str, Any]:
    if raw is None:
        return {}
    if isinstance(raw, dict):
        return raw
    if isinstance(raw, str):
        s = raw.strip()
        if not s:
            return {}
        try:
            obj = json.loads(s)
            return obj if isinstance(obj, dict) else {}
        except json.JSONDecodeError:
            return {}
    return {}


def _build_namespace() -> dict[str, Any]:
    return {
        "scrapy": scrapy,
        "SentimentItem": SentimentItem,
        "datetime": datetime,
        "timedelta": timedelta,
        "json": json,
        "re": re,
        "pydantic": pydantic,
        "BaseModel": BaseModel,
        "Field": Field,
        "ValidationError": ValidationError,
    }


def _lint_ast(tree: ast.AST) -> list[str]:
    warnings: list[str] = []
    for node in ast.walk(tree):
        if isinstance(node, (ast.Import, ast.ImportFrom)):
            warnings.append("无需编写 import；运行时会自动注入 scrapy/json/pydantic 等常用库")
        if isinstance(node, ast.Call):
            if isinstance(node.func, ast.Name) and node.func.id in {"exec", "eval", "__import__"}:
                warnings.append(f"检测到高风险调用 `{node.func.id}`，建议移除")
    return warnings


def compile_spider_code(
    spider_code: str,
    spider_class: Optional[str] = None,
    spider_name: Optional[str] = None,
) -> tuple[type, list[str]]:
    if not spider_code or not spider_code.strip():
        raise ValueError("spiderCode 不能为空")

    try:
        tree = ast.parse(spider_code)
    except SyntaxError as e:
        raise ValueError(f"Spider 代码语法错误: {e}") from e

    warnings = _lint_ast(tree)

    namespace = _build_namespace()
    try:
        exec(spider_code, namespace, namespace)
    except Exception as e:
        raise ValueError(f"Spider 代码执行失败: {e}") from e

    candidates = [
        obj
        for obj in namespace.values()
        if isinstance(obj, type) and issubclass(obj, scrapy.Spider) and obj is not scrapy.Spider
    ]
    if not candidates:
        raise ValueError("spiderCode 未定义 scrapy.Spider 子类")

    if spider_class:
        cls = namespace.get(spider_class)
        if not isinstance(cls, type) or cls not in candidates:
            raise ValueError(f"spiderClass={spider_class} 未找到 scrapy.Spider 子类")
        spider_cls = cls
    else:
        spider_cls = candidates[0]

    if not getattr(spider_cls, "name", None):
        spider_cls.name = spider_name or f"db_spider_{spider_cls.__name__.lower()}"

    return spider_cls, warnings


def validate_spider_code(
    spider_code: str,
    spider_class: Optional[str] = None,
    spider_name: Optional[str] = None,
) -> dict[str, Any]:
    spider_cls, warnings = compile_spider_code(spider_code, spider_class, spider_name)
    return {
        "ok": True,
        "className": spider_cls.__name__,
        "spiderName": getattr(spider_cls, "name", ""),
        "warnings": warnings,
    }

