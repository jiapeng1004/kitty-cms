"""
情感分析端点
"""

from datetime import datetime
from enum import Enum
from typing import Optional

from flask import request, jsonify
from sqlalchemy import asc, desc, or_
from sqlalchemy.orm import joinedload

from src.api.v1 import api_v1
from src.schemas import (
    SentimentCreate,
    SentimentUpdate,
    SentimentResponse,
    PaginationResponse,
)
from src.models.sentiment import Sentiment, Source


def _parse_dt(value: Optional[str]) -> Optional[datetime]:
    if not value:
        return None
    try:
        if value.endswith("Z"):
            value = value[:-1] + "+00:00"
        return datetime.fromisoformat(value)
    except ValueError:
        return None


def _sentiment_to_response(s: Sentiment) -> SentimentResponse:
    src = s.source
    return SentimentResponse(
        id=s.id,
        source_id=s.source_id,
        title=s.title,
        content=s.content or "",
        url=s.url,
        author=s.author,
        publish_time=s.publish_time,
        sentiment_score=s.sentiment_score or 0.0,
        sentiment_label=s.sentiment_label or "neutral",
        keywords=s.keywords,
        views=s.views or 0,
        likes=s.likes or 0,
        comments=s.comments or 0,
        source_name=src.name if src else None,
        created_at=s.created_at,
        updated_at=s.updated_at,
    )


@api_v1.route("/sentiments", methods=["GET"])
def list_sentiments():
    """查询舆情列表"""
    from src.main import app

    db = app.session_factory()

    try:
        page = request.args.get("page", 1, type=int)
        page_size = request.args.get("page_size", 20, type=int)
        source_id = request.args.get("source_id", type=int)
        sentiment_label = request.args.get("sentiment_label", type=str)
        keyword = request.args.get("keyword", type=str)
        start_time = _parse_dt(request.args.get("start_time", type=str))
        end_time = _parse_dt(request.args.get("end_time", type=str))
        sort = request.args.get("sort", "publish_time", type=str) or "publish_time"
        order = (request.args.get("order", "desc", type=str) or "desc").lower()

        query = db.query(Sentiment)

        if source_id:
            query = query.filter(Sentiment.source_id == source_id)

        if sentiment_label:
            query = query.filter(Sentiment.sentiment_label == sentiment_label)

        if keyword:
            kw = keyword.strip()
            if kw:
                like = f"%{kw}%"
                query = query.filter(
                    or_(
                        Sentiment.title.like(like),
                        Sentiment.keywords.like(like),
                    )
                )

        if start_time:
            query = query.filter(Sentiment.publish_time >= start_time)
        if end_time:
            query = query.filter(Sentiment.publish_time <= end_time)

        total = query.count()

        sort_col = Sentiment.publish_time
        if sort == "views":
            sort_col = Sentiment.views
        elif sort == "sentiment_score":
            sort_col = Sentiment.sentiment_score

        if order == "asc":
            query = query.order_by(asc(sort_col))
        else:
            query = query.order_by(desc(sort_col))

        offset = (page - 1) * page_size
        items = (
            query.options(joinedload(Sentiment.source)).offset(offset).limit(page_size).all()
        )

        total_pages = (total + page_size - 1) // page_size if page_size else 0
        has_next = page < total_pages
        has_prev = page > 1

        pagination = PaginationResponse(
            total=total,
            page=page,
            page_size=page_size,
            total_pages=total_pages,
            has_next=has_next,
            has_prev=has_prev,
        )

        sentiment_responses = [_sentiment_to_response(s) for s in items]

        return jsonify(
            {
                "items": [m.model_dump(mode="json", by_alias=True) for m in sentiment_responses],
                "pagination": pagination.model_dump(mode="json", by_alias=True),
            }
        )
    finally:
        db.close()


@api_v1.route("/sentiments", methods=["POST"])
def create_sentiment():
    """创建舆情"""
    from src.main import app

    db = app.session_factory()

    try:
        data = request.json
        sentiment = SentimentCreate(**data)

        payload = sentiment.model_dump()
        sl = payload.get("sentiment_label")
        if isinstance(sl, Enum):
            payload["sentiment_label"] = sl.value

        db_sentiment = Sentiment(**payload)
        db.add(db_sentiment)
        db.commit()
        db.refresh(db_sentiment)
        db_sentiment = (
            db.query(Sentiment)
            .options(joinedload(Sentiment.source))
            .filter(Sentiment.id == db_sentiment.id)
            .first()
        )

        response = _sentiment_to_response(db_sentiment)

        return jsonify(response.model_dump(mode="json", by_alias=True)), 201

    except Exception as e:
        db.rollback()
        return jsonify({"error": str(e)}), 400
    finally:
        db.close()


@api_v1.route("/sentiments/<int:sentiment_id>", methods=["GET"])
def get_sentiment(sentiment_id: int):
    """查询舆情详情"""
    from src.main import app

    db = app.session_factory()

    try:
        sentiment = (
            db.query(Sentiment)
            .options(joinedload(Sentiment.source))
            .filter(Sentiment.id == sentiment_id)
            .first()
        )

        if not sentiment:
            return jsonify({"error": "Sentiment not found"}), 404

        response = _sentiment_to_response(sentiment)

        return jsonify(response.model_dump(mode="json", by_alias=True))
    finally:
        db.close()


@api_v1.route("/sentiments/<int:sentiment_id>", methods=["PUT"])
def update_sentiment(sentiment_id: int):
    """更新舆情"""
    from src.main import app

    db = app.session_factory()

    try:
        sentiment = db.query(Sentiment).filter(Sentiment.id == sentiment_id).first()

        if not sentiment:
            return jsonify({"error": "Sentiment not found"}), 404

        data = request.json
        update_data = SentimentUpdate(**data)

        for key, value in update_data.model_dump(exclude_unset=True).items():
            if key == "sentiment_label" and isinstance(value, Enum):
                value = value.value
            setattr(sentiment, key, value)

        db.commit()
        db.refresh(sentiment)
        sentiment = (
            db.query(Sentiment)
            .options(joinedload(Sentiment.source))
            .filter(Sentiment.id == sentiment_id)
            .first()
        )

        response = _sentiment_to_response(sentiment)

        return jsonify(response.model_dump(mode="json", by_alias=True))

    except Exception as e:
        db.rollback()
        return jsonify({"error": str(e)}), 400
    finally:
        db.close()


@api_v1.route("/sentiments/<int:sentiment_id>", methods=["DELETE"])
def delete_sentiment(sentiment_id: int):
    """删除舆情"""
    from src.main import app

    db = app.session_factory()

    try:
        sentiment = db.query(Sentiment).filter(Sentiment.id == sentiment_id).first()

        if not sentiment:
            return jsonify({"error": "Sentiment not found"}), 404

        db.delete(sentiment)
        db.commit()

        return jsonify({"message": "Sentiment deleted successfully"}), 200
    finally:
        db.close()
