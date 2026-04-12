"""
Flask应用入口
"""
from flask import Flask
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from src.config import settings
from src.api.v1 import register_routes
from src.db.migrate import ensure_schema, ensure_tables, seed_default_sources


def create_app() -> Flask:
    """创建Flask应用"""
    app = Flask(__name__)

    # 配置
    app.config["DEBUG"] = settings.debug
    app.config["SECRET_KEY"] = "your-secret-key-here"

    # 数据库配置
    engine = create_engine(
        settings.database.connection_url,
        echo=settings.debug,
        pool_pre_ping=True,
        pool_recycle=3600
    )
    ensure_tables(engine)
    ensure_schema(engine)
    seed_default_sources(engine)
    app.engine = engine
    app.session_factory = sessionmaker(bind=engine)
    # 注册路由
    register_routes(app)
    return app


# 供各 endpoint `from src.main import app` 获取会话工厂等；与 `python -m src.main` 共用同一实例
app = create_app()


if __name__ == "__main__":
    app.run(
        host=settings.server.host,
        port=settings.server.port,
        debug=settings.debug
    )
