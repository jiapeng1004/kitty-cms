"""
系统设置：GET 返回当前环境与可展示配置；POST 将非敏感项写入本地 JSON（演示用）。
"""

import json
from pathlib import Path

from flask import request, jsonify

from src.api.v1 import api_v1
from src.config.settings import get_settings


def _project_root() -> Path:
    return Path(__file__).resolve().parents[4]


def _settings_file() -> Path:
    root = _project_root()
    data = root / "data"
    data.mkdir(parents=True, exist_ok=True)
    return data / "app_settings.json"


def _load_overrides() -> dict:
    path = _settings_file()
    if not path.is_file():
        return {}
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return {}


def _mask(s: str) -> str:
    if not s:
        return ""
    if len(s) <= 2:
        return "**"
    return s[0] + "*" * (len(s) - 2) + s[-1]


@api_v1.route("/settings", methods=["GET"])
def get_settings_api():
    s = get_settings()
    overrides = _load_overrides()
    payload = {
        "databaseType": s.database.type,
        "databaseHost": s.database.host or "",
        "databasePort": int(s.database.port or 0),
        "databaseName": s.database.name,
        "databaseUser": s.database.user or "",
        "databasePassword": _mask(s.database.password or ""),
        "crawlerTimeout": s.crawler.timeout,
        "maxRetries": s.crawler.max_retries,
        "enableSentimentAnalysis": overrides.get("enableSentimentAnalysis", True),
        "enableKeywordExtraction": overrides.get("enableKeywordExtraction", True),
    }
    return jsonify(payload)


@api_v1.route("/settings", methods=["POST"])
def save_settings_api():
    data = request.get_json() or {}
    path = _settings_file()
    overrides = _load_overrides()
    for key in ("enableSentimentAnalysis", "enableKeywordExtraction"):
        if key in data:
            overrides[key] = bool(data[key])
    path.write_text(json.dumps(overrides, ensure_ascii=False, indent=2), encoding="utf-8")
    return jsonify({"saved": True, "message": "已保存开关类配置；数据库连接等请通过环境变量修改"})
