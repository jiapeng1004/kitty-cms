"""
信息源端点
"""

import json
import os

from flask import request, jsonify
from sqlalchemy import or_

from src.api.v1 import api_v1
from src.schemas import (
    SourceCreate, SourceUpdate, SourceResponse, PaginationResponse
)
from src.models.sentiment import Source
from src.utils.spider_code import parse_source_config, validate_spider_code


def _strip_spider_name_from_config(cfg: dict) -> dict:
    """爬虫标识只认 sources.name，config 内不再存 spiderName / spider_name。"""
    cfg = dict(cfg)
    cfg.pop("spiderName", None)
    cfg.pop("spider_name", None)
    return cfg


def _sync_source_payload_name_only(payload: dict) -> dict:
    """落库前：config 去掉 spider 名字段；列 spider_name 与 name 保持一致（便于日志/历史，语义等同 name）。"""
    name = (payload.get("name") or "").strip()
    if not name:
        return payload
    cfg = _strip_spider_name_from_config(parse_source_config(payload.get("config")))
    payload["config"] = cfg
    payload["spider_name"] = name
    return payload


def _prepare_source_row(payload: dict) -> dict:
    d = dict(payload)
    if "enabled" in d and d["enabled"] is not None:
        d["enabled"] = 1 if bool(d["enabled"]) else 0
    cfg = d.get("config")
    if isinstance(cfg, dict):
        d["config"] = json.dumps(cfg, ensure_ascii=False)
    return d


def _source_to_response(source: Source) -> SourceResponse:
    cfg = _strip_spider_name_from_config(parse_source_config(source.config))
    return SourceResponse(
        id=source.id,
        name=source.name,
        url=source.url,
        type=source.type,
        spider_name=source.name,
        category=source.category,
        enabled=bool(source.enabled),
        config=cfg,
        created_at=source.created_at,
        updated_at=source.updated_at,
    )


def _validate_source_spider_config(cfg: dict, display_name: str) -> tuple[bool, dict]:
    """display_name 即信息源 name，与 Scrapy spider 名、动态类注入名完全一致。"""
    spider_name = (display_name or "").strip()
    if not spider_name:
        return False, {"error": "名称不能为空（作为唯一 Spider 标识）"}

    cfg = _strip_spider_name_from_config(cfg)
    impl_mode = str(cfg.get("implementation") or cfg.get("implMode") or "").strip().lower()
    if not impl_mode:
        impl_mode = "db" if (cfg.get("spiderCode") or cfg.get("spider_code")) else "local"
    cfg["implementation"] = impl_mode

    if impl_mode == "local":
        try:
            os.environ.setdefault("SCRAPY_SETTINGS_MODULE", "src.scrapy_settings")
            from scrapy.spiderloader import SpiderLoader
            from scrapy.utils.project import get_project_settings

            loader = SpiderLoader.from_settings(get_project_settings())
            loader.load(spider_name)
            return True, {
                "ok": True,
                "mode": "local",
                "spiderName": spider_name,
                "warnings": [],
            }
        except Exception as e:
            return False, {"error": f"本地 Spider 无效（名称需与 src/spiders 中一致）: {spider_name} ({e})"}

    code = cfg.get("spiderCode") or cfg.get("spider_code")
    if not code:
        return False, {"error": "source.config.spiderCode 不能为空（当前为 DB Spider 模式）"}
    class_name = cfg.get("spiderClass") or cfg.get("spider_class")
    try:
        result = validate_spider_code(code, spider_class=class_name, spider_name=spider_name)
        result["mode"] = "db"
        return True, result
    except Exception as e:
        return False, {"error": str(e)}


def _type_filter_clause(tag: str):
    """按逗号分隔标签之一匹配（整段匹配，避免子串误伤）。"""
    t = tag.strip()
    if not t:
        return None
    return or_(
        Source.type == t,
        Source.type.like(f"{t},%"),
        Source.type.like(f"%,{t},%"),
        Source.type.like(f"%,{t}"),
    )


@api_v1.route("/sources/type-tag-suggestions", methods=["GET"])
def source_type_tag_suggestions():
    """统计已有信息源 type 字段中逗号分隔标签的词频，返回 Top N（默认 5，最大 5）。"""
    from src.main import app

    limit = request.args.get("limit", 5, type=int)
    if limit is None:
        limit = 5
    limit = max(0, min(limit, 5))

    db = app.session_factory()
    try:
        rows = db.query(Source.type).filter(Source.type.isnot(None), Source.type != "").all()
        counts: dict[str, int] = {}
        for (raw,) in rows:
            if not raw:
                continue
            text_val = str(raw).replace("，", ",")
            for part in text_val.split(","):
                w = part.strip()
                if not w:
                    continue
                counts[w] = counts.get(w, 0) + 1
        top = sorted(counts.items(), key=lambda x: (-x[1], x[0]))[:limit]
        return jsonify({"items": [{"tag": k, "count": v} for k, v in top]})
    finally:
        db.close()


@api_v1.route("/sources/validate-spider", methods=["POST"])
def validate_source_spider():
    """校验信息源里配置的 spiderCode/spiderClass（不落库）。Spider 名 = 请求体 name。"""
    data = request.get_json() or {}
    display_name = (data.get("name") or "").strip()
    raw_cfg = data.get("config")
    if raw_cfg is None:
        raw_cfg = {k: v for k, v in data.items() if k != "name"}
    cfg = parse_source_config(raw_cfg)
    ok, res = _validate_source_spider_config(cfg, display_name)
    status = 200
    if ok:
        return jsonify({"ok": True, **res}), status
    return jsonify({"ok": False, **res}), status


@api_v1.route("/sources", methods=["GET"])
def list_sources():
    """查询信息源列表"""
    from src.main import app
    
    db = app.session_factory()
    
    try:
        # 解析查询参数
        page = request.args.get("page", 1, type=int)
        page_size = request.args.get("page_size", 20, type=int)
        enabled = request.args.get("enabled", type=str)
        source_type = request.args.get("type", type=str)
        
        # 构建查询
        query = db.query(Source)
        
        if enabled is not None:
            query = query.filter(Source.enabled == (1 if enabled.lower() == "true" else 0))
        
        if source_type:
            clause = _type_filter_clause(source_type)
            if clause is not None:
                query = query.filter(clause)
        
        # 获取总数
        total = query.count()
        
        # 分页
        offset = (page - 1) * page_size
        items = query.offset(offset).limit(page_size).all()
        
        # 计算分页信息
        total_pages = (total + page_size - 1) // page_size
        has_next = page < total_pages
        has_prev = page > 1
        
        pagination = PaginationResponse(
            total=total,
            page=page,
            page_size=page_size,
            total_pages=total_pages,
            has_next=has_next,
            has_prev=has_prev
        )
        
        # 转换为响应格式
        source_responses = [_source_to_response(item) for item in items]
        
        return jsonify(
            {
                "items": [s.model_dump(mode="json", by_alias=True) for s in source_responses],
                "pagination": pagination.model_dump(mode="json", by_alias=True),
            }
        )
    finally:
        db.close()


@api_v1.route("/sources", methods=["POST"])
def create_source():
    """创建信息源"""
    from src.main import app
    
    db = app.session_factory()
    
    try:
        data = request.json
        source = SourceCreate(**data)
        payload = source.model_dump()
        display_name = (payload.get("name") or "").strip()
        payload = _sync_source_payload_name_only(payload)
        cfg = parse_source_config(payload.get("config"))
        ok, res = _validate_source_spider_config(cfg, display_name)
        if not ok:
            return jsonify(res), 400
        payload["config"] = cfg

        db_source = Source(**_prepare_source_row(payload))
        db.add(db_source)
        db.commit()
        db.refresh(db_source)

        response = _source_to_response(db_source)
        return jsonify(response.model_dump(mode="json", by_alias=True)), 201
        
    except Exception as e:
        db.rollback()
        return jsonify({"error": str(e)}), 400
    finally:
        db.close()


@api_v1.route("/sources/<int:source_id>", methods=["GET"])
def get_source(source_id: int):
    """查询信息源详情"""
    from src.main import app
    
    db = app.session_factory()
    
    try:
        source = db.query(Source).filter(Source.id == source_id).first()
        
        if not source:
            return jsonify({"error": "Source not found"}), 404
        
        response = _source_to_response(source)
        
        return jsonify(response.model_dump(mode="json", by_alias=True))
    finally:
        db.close()


@api_v1.route("/sources/<int:source_id>", methods=["PUT"])
def update_source(source_id: int):
    """更新信息源"""
    from src.main import app
    
    db = app.session_factory()
    
    try:
        source = db.query(Source).filter(Source.id == source_id).first()
        
        if not source:
            return jsonify({"error": "Source not found"}), 404
        
        data = request.json
        update_data = SourceUpdate(**data)
        patch = update_data.model_dump(exclude_unset=True)

        display_name = str(
            patch["name"] if "name" in patch else source.name or "",
        ).strip()
        if not display_name:
            return jsonify({"error": "名称不能为空"}), 400

        if "config" in patch:
            cfg = parse_source_config(patch["config"])
        else:
            cfg = parse_source_config(source.config)
        cfg = _strip_spider_name_from_config(cfg)
        ok, res = _validate_source_spider_config(cfg, display_name)
        if not ok:
            return jsonify(res), 400
        patch["config"] = cfg
        patch["spider_name"] = display_name
        if "name" in patch:
            patch["name"] = display_name

        for key, value in _prepare_source_row(patch).items():
            setattr(source, key, value)

        db.commit()
        db.refresh(source)

        response = _source_to_response(source)
        return jsonify(response.model_dump(mode="json", by_alias=True))
        
    except Exception as e:
        db.rollback()
        return jsonify({"error": str(e)}), 400
    finally:
        db.close()


@api_v1.route("/sources/<int:source_id>", methods=["DELETE"])
def delete_source(source_id: int):
    """删除信息源"""
    from src.main import app
    
    db = app.session_factory()
    
    try:
        source = db.query(Source).filter(Source.id == source_id).first()
        
        if not source:
            return jsonify({"error": "Source not found"}), 404
        
        db.delete(source)
        db.commit()
        
        return jsonify({"message": "Source deleted successfully"}), 200
    finally:
        db.close()
