"""
任务管理API
"""

import csv
import io
import json
from flask import request, jsonify, Response
from datetime import datetime
from typing import Any, Optional, Tuple

from src.api.v1 import api_v1
from src.models import Task, TaskExecution
from src.models.sentiment import Source
from src.services.crawler import CrawlerService
from src.utils.cron_validate import normalize_task_frequency


def _load_json_dict(raw: object) -> dict:
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


def _normalize_runtime_config(raw: object) -> dict:
    data = _load_json_dict(raw)
    if not data:
        return {"robotstxtObey": True, "cookiesEnabled": False}
    return {
        "robotstxtObey": bool(data.get("robotstxtObey", data.get("ROBOTSTXT_OBEY", True))),
        "cookiesEnabled": bool(data.get("cookiesEnabled", data.get("COOKIES_ENABLED", False))),
    }


def _execution_to_dict(execution: TaskExecution) -> dict:
    return {
        "id": execution.id,
        "taskId": execution.task_id,
        "triggerMode": execution.trigger_mode,
        "status": execution.status,
        "spiderName": execution.spider_name,
        "taskType": execution.task_type,
        "sourceId": execution.source_id,
        "sourceName": execution.source_name,
        "startedAt": execution.started_at.isoformat() if execution.started_at else None,
        "finishedAt": execution.finished_at.isoformat() if execution.finished_at else None,
        "itemsCount": execution.items_count or 0,
        "requestCount": execution.request_count or 0,
        "errorMessage": execution.error_message,
        "errorTrace": execution.error_trace,
        "createdAt": execution.created_at.isoformat() if execution.created_at else None,
    }


def _apply_execution_filters(
    query: Any,
    status: Optional[str],
    start_at: Optional[str],
    end_at: Optional[str],
) -> Tuple[Any, Optional[Tuple[Any, int]]]:
    """对 TaskExecution 查询追加筛选；无效时间则返回 (jsonify, status_code)。"""
    if status:
        query = query.filter(TaskExecution.status == status.strip())
    if start_at:
        try:
            query = query.filter(TaskExecution.started_at >= datetime.fromisoformat(start_at))
        except ValueError:
            return query, (jsonify({"error": "start_at 不是有效的 ISO 时间"}), 400)
    if end_at:
        try:
            query = query.filter(TaskExecution.started_at <= datetime.fromisoformat(end_at))
        except ValueError:
            return query, (jsonify({"error": "end_at 不是有效的 ISO 时间"}), 400)
    return query, None


@api_v1.route("/tasks", methods=["GET"])
def get_tasks():
    """获取任务列表"""
    from src.main import app

    db = app.session_factory()
    try:
        tasks = db.query(Task).all()
        latest_exec_map: dict[int, TaskExecution] = {}
        for row in (
            db.query(TaskExecution)
            .order_by(TaskExecution.task_id.asc(), TaskExecution.id.desc())
            .all()
        ):
            if row.task_id not in latest_exec_map:
                latest_exec_map[row.task_id] = row
        return jsonify(
            {
                "items": [
                    {
                        "id": task.id,
                        "name": task.name,
                        "type": task.type,
                        "sourceId": task.source_id,
                        "sourceName": task.source_name,
                        "status": task.status,
                        "lastRun": task.last_run.isoformat() if task.last_run else None,
                        "nextRun": task.next_run.isoformat() if task.next_run else None,
                        "frequency": task.frequency,
                        "runtimeConfig": _normalize_runtime_config(task.runtime_config),
                        "lastExecution": _execution_to_dict(latest_exec_map[task.id])
                        if task.id in latest_exec_map
                        else None,
                    }
                    for task in tasks
                ]
            }
        )
    finally:
        db.close()


@api_v1.route("/tasks/validate-frequency", methods=["POST"])
def validate_task_frequency():
    """校验爬取频率（Cron 5 段或 manual），供前端表单实时校验。"""
    data = request.get_json() or {}
    raw = data.get("frequency", "")
    try:
        normalized = normalize_task_frequency(raw)
        return jsonify({"ok": True, "frequency": normalized})
    except ValueError as e:
        return jsonify({"ok": False, "error": str(e)}), 200


@api_v1.route("/tasks", methods=["POST"])
def create_task():
    """创建任务"""
    from src.main import app

    data = request.get_json()
    db = app.session_factory()
    try:
        source_id = data.get("sourceId") or data.get("source_id")
        source_name = data.get("sourceName") or data.get("source_name")
        task_type = data.get("type") or data.get("task_type", "hotlist")

        if not data.get("name"):
            return jsonify({"error": "任务名称不能为空"}), 400
        if not source_id:
            return jsonify({"error": "请选择信息源"}), 400

        try:
            frequency = normalize_task_frequency(data.get("frequency"))
        except ValueError as e:
            return jsonify({"error": str(e)}), 400
        runtime_config = _normalize_runtime_config(
            data.get("runtimeConfig") or data.get("runtime_config")
        )

        if not source_name:
            source = db.query(Source).filter(Source.id == source_id).first()
            if source:
                source_name = source.name
            else:
                return jsonify({"error": "信息源不存在"}), 400

        task = Task(
            name=data["name"],
            type=task_type,
            source_id=source_id,
            source_name=source_name,
            status="stopped",
            frequency=frequency,
            runtime_config=json.dumps(runtime_config, ensure_ascii=False),
        )
        db.add(task)
        db.commit()
        return jsonify({"id": task.id, "status": "success"})
    except Exception as e:
        db.rollback()
        return jsonify({"error": f"创建失败: {str(e)}"}), 500
    finally:
        db.close()


@api_v1.route("/tasks/<int:task_id>", methods=["PUT"])
def update_task(task_id):
    """更新任务"""
    from src.main import app

    data = request.get_json() or {}
    db = app.session_factory()
    try:
        task = db.query(Task).filter(Task.id == task_id).first()
        if not task:
            return jsonify({"error": "任务不存在"}), 404

        if "name" in data and data["name"]:
            task.name = data["name"]
        if "type" in data and data["type"]:
            task.type = data["type"]
        sid = data.get("sourceId") or data.get("source_id")
        if sid is not None:
            source = db.query(Source).filter(Source.id == sid).first()
            if not source:
                return jsonify({"error": "信息源不存在"}), 400
            task.source_id = sid
            task.source_name = source.name
        if "frequency" in data:
            try:
                task.frequency = normalize_task_frequency(data["frequency"])
            except ValueError as e:
                return jsonify({"error": str(e)}), 400
        if "runtimeConfig" in data or "runtime_config" in data:
            runtime_config = _normalize_runtime_config(
                data.get("runtimeConfig") or data.get("runtime_config")
            )
            task.runtime_config = json.dumps(runtime_config, ensure_ascii=False)

        db.commit()
        return jsonify({"status": "success"})
    except Exception as e:
        db.rollback()
        return jsonify({"error": str(e)}), 500
    finally:
        db.close()


@api_v1.route("/tasks/<int:task_id>/start", methods=["POST"])
def start_task(task_id):
    """启动任务"""
    from src.main import app

    db = app.session_factory()
    try:
        task = db.query(Task).filter(Task.id == task_id).first()
        if not task:
            return jsonify({"error": "任务不存在"}), 404

        source = db.query(Source).filter(Source.id == task.source_id).first()
        if not source:
            return jsonify({"error": "信息源不存在"}), 404

        execution = TaskExecution(
            task_id=task.id,
            trigger_mode="manual",
            status="queued",
            task_type=task.type,
            source_id=source.id,
            source_name=source.name,
            started_at=datetime.now(),
            spider_name=source.name,
        )
        db.add(execution)
        db.flush()

        # 进程内 CrawlerRunner 队列执行，不阻塞 HTTP
        task_cfg = _normalize_runtime_config(task.runtime_config)
        out = CrawlerService().crawl_source(
            source,
            max_pages=10,
            runtime_config=task_cfg,
            task_context={
                "task_id": task.id,
                "task_type": task.type,
                "trigger_mode": "manual",
                "task_execution_id": execution.id,
            },
        )
        if not out.success:
            execution.status = "failed"
            execution.finished_at = datetime.now()
            execution.error_message = out.message or "; ".join(out.errors or [])
            task.status = "failed"
            db.commit()
            return jsonify({"error": out.message, "details": out.errors}), 400

        task.status = "running"
        task.last_run = datetime.now()
        execution.status = "running"
        db.commit()

        return jsonify(
            {
                "status": "success",
                "message": out.message,
                "executionId": execution.id,
            }
        )
    finally:
        db.close()


@api_v1.route("/tasks/<int:task_id>/stop", methods=["POST"])
def stop_task(task_id):
    """停止任务"""
    from src.main import app

    db = app.session_factory()
    try:
        task = db.query(Task).filter(Task.id == task_id).first()
        if not task:
            return jsonify({"error": "任务不存在"}), 404

        task.status = "stopped"
        db.commit()
        return jsonify({"status": "success"})
    finally:
        db.close()


@api_v1.route("/tasks/<int:task_id>", methods=["DELETE"])
def delete_task(task_id):
    """删除任务"""
    from src.main import app

    db = app.session_factory()
    try:
        task = db.query(Task).filter(Task.id == task_id).first()
        if not task:
            return jsonify({"error": "任务不存在"}), 404

        db.query(TaskExecution).filter(TaskExecution.task_id == task_id).delete()
        db.delete(task)
        db.commit()
        return jsonify({"status": "success"})
    finally:
        db.close()


@api_v1.route("/tasks/<int:task_id>/executions", methods=["GET"])
def get_task_executions(task_id: int):
    """查询任务执行历史。"""
    from src.main import app

    db = app.session_factory()
    try:
        task = db.query(Task).filter(Task.id == task_id).first()
        if not task:
            return jsonify({"error": "任务不存在"}), 404

        page = request.args.get("page", 1, type=int)
        page_size = request.args.get("page_size", 20, type=int)
        status = request.args.get("status", type=str)
        start_at = request.args.get("start_at", type=str)
        end_at = request.args.get("end_at", type=str)
        page = 1 if page < 1 else page
        page_size = 20 if page_size < 1 else min(page_size, 100)

        query = db.query(TaskExecution).filter(TaskExecution.task_id == task_id)
        query, err = _apply_execution_filters(query, status, start_at, end_at)
        if err is not None:
            return err
        total = query.count()
        offset = (page - 1) * page_size
        rows = (
            query.order_by(TaskExecution.id.desc())
            .offset(offset)
            .limit(page_size)
            .all()
        )
        total_pages = (total + page_size - 1) // page_size if total > 0 else 0
        return jsonify(
            {
                "items": [_execution_to_dict(x) for x in rows],
                "pagination": {
                    "total": total,
                    "page": page,
                    "pageSize": page_size,
                    "totalPages": total_pages,
                    "hasNext": page < total_pages,
                    "hasPrev": page > 1,
                },
            }
        )
    finally:
        db.close()


@api_v1.route("/tasks/<int:task_id>/executions/export", methods=["GET"])
def export_task_executions_csv(task_id: int):
    """导出任务执行历史为 CSV（UTF-8 BOM，便于 Excel 打开），支持与列表相同的筛选。"""
    from src.main import app

    db = app.session_factory()
    try:
        task = db.query(Task).filter(Task.id == task_id).first()
        if not task:
            return jsonify({"error": "任务不存在"}), 404

        status = request.args.get("status", type=str)
        start_at = request.args.get("start_at", type=str)
        end_at = request.args.get("end_at", type=str)

        query = db.query(TaskExecution).filter(TaskExecution.task_id == task_id)
        query, err = _apply_execution_filters(query, status, start_at, end_at)
        if err is not None:
            return err

        rows = query.order_by(TaskExecution.id.desc()).all()

        buf = io.StringIO()
        writer = csv.writer(buf)
        writer.writerow(
            [
                "id",
                "task_id",
                "trigger_mode",
                "status",
                "spider_name",
                "task_type",
                "source_id",
                "source_name",
                "started_at",
                "finished_at",
                "items_count",
                "request_count",
                "error_message",
                "error_trace",
                "created_at",
            ]
        )
        for r in rows:
            writer.writerow(
                [
                    r.id,
                    r.task_id,
                    r.trigger_mode or "",
                    r.status or "",
                    r.spider_name or "",
                    r.task_type or "",
                    r.source_id if r.source_id is not None else "",
                    r.source_name or "",
                    r.started_at.isoformat() if r.started_at else "",
                    r.finished_at.isoformat() if r.finished_at else "",
                    r.items_count or 0,
                    r.request_count or 0,
                    r.error_message or "",
                    r.error_trace or "",
                    r.created_at.isoformat() if r.created_at else "",
                ]
            )

        payload = "\ufeff" + buf.getvalue()
        filename = f"task-{task_id}-executions.csv"
        return Response(
            payload,
            mimetype="text/csv; charset=utf-8",
            headers={"Content-Disposition": f'attachment; filename="{filename}"'},
        )
    finally:
        db.close()
