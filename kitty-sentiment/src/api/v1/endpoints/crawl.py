"""
爬虫端点
"""

from flask import request, jsonify

from src.api.v1 import api_v1
from src.schemas import CrawlRequest, CrawlResponse
from src.services.crawler import CrawlerService


@api_v1.route("/crawl", methods=["POST"])
def crawl():
    """手动触发爬取"""
    try:
        data = request.json
        crawl_request = CrawlRequest(**data)

        crawler = CrawlerService()

        if crawl_request.source_id:
            from src.models.sentiment import Source
            from src.main import app

            db = app.session_factory()

            try:
                source = db.query(Source).filter(Source.id == crawl_request.source_id).first()

                if not source:
                    return jsonify({"error": "Source not found"}), 404

                response = crawler.crawl_source(source, crawl_request.max_pages)
            finally:
                db.close()
        elif crawl_request.url:
            result = crawler.crawl_single_url(crawl_request.url)
            response = CrawlResponse(
                success=bool(result.get("success")),
                message=result.get("error", ""),
                crawled_count=0,
                new_items=0,
                errors=[result.get("error", "Unknown error")],
            )
        else:
            return jsonify({"error": "source_id or url is required"}), 400

        return jsonify(response.model_dump(mode="json", by_alias=True))

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 400


@api_v1.route("/crawl/status", methods=["GET"])
def crawl_status():
    """爬取状态"""
    return jsonify(
        {
            "status": "running",
            "crawlers": 0,
            "pending": 0,
            "completed": 0,
            "failed": 0,
        }
    )
