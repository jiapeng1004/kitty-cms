"""
API v1路由
"""
import json
from functools import lru_cache

from flask import Blueprint, Flask

api_v1 = Blueprint("api/v1", __name__)
from src.api.v1.endpoints import sentiment, sources, crawl, tasks, stats, auth, system



def register_routes(app: Flask):
    """注册API路由"""
    # 注册蓝图
    app.register_blueprint(api_v1, url_prefix="/api/v1")

    # 根路径
    @app.route("/")
    def index():
        @lru_cache
        def url_map_to_json(app):
            routes = []
            for rule in app.url_map.iter_rules():
                # 过滤掉 HEAD、OPTIONS，只保留真实业务方法
                methods = [m for m in rule.methods if m not in ['HEAD', 'OPTIONS']]

                routes.append({
                    "endpoint": rule.endpoint,  # 函数名
                    "path": str(rule),  # API 路径
                    "methods": methods  # 允许的请求方法
                })
            return json.dumps(routes, ensure_ascii=False, indent=2)

        return url_map_to_json(app)