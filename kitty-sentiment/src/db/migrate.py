"""
轻量级 schema：首次启动创建表；已有库则按需 ALTER 补列（无 Alembic 时可用）。
空库时写入默认信息源，与 src/spiders 中 source_id=1..4 一致。
"""

from sqlalchemy import inspect, text
from sqlalchemy.engine import Engine
from sqlalchemy.orm import sessionmaker


def ensure_tables(engine: Engine) -> None:
    """根据 ORM 模型创建尚未存在的表（空库或缺表时）。"""
    from src.models.base import Base
    from src.models.sentiment import Sentiment, Source  # noqa: F401
    from src.models.task import Task  # noqa: F401
    from src.models.task_execution import TaskExecution  # noqa: F401

    Base.metadata.create_all(engine)


def ensure_schema(engine: Engine) -> None:
    dialect = engine.dialect.name
    insp = inspect(engine)

    if "sentiments" in insp.get_table_names():
        cols = {c["name"] for c in insp.get_columns("sentiments")}
        statements = []
        if "views" not in cols:
            statements.append("ADD COLUMN views INTEGER DEFAULT 0")
        if "likes" not in cols:
            statements.append("ADD COLUMN likes INTEGER DEFAULT 0")
        if "comments" not in cols:
            statements.append("ADD COLUMN comments INTEGER DEFAULT 0")
        if statements:
            if dialect == "sqlite":
                # SQLite 每次 ALTER 一列
                for stmt in statements:
                    with engine.begin() as conn:
                        conn.execute(text(f"ALTER TABLE sentiments {stmt}"))
            else:
                # MySQL 等可一次 ALTER 多列（若不支持则拆开）
                for stmt in statements:
                    with engine.begin() as conn:
                        conn.execute(text(f"ALTER TABLE sentiments {stmt}"))

    if "sources" in insp.get_table_names():
        cols = {c["name"] for c in insp.get_columns("sources")}
        if "spider_name" not in cols:
            stmt = "ADD COLUMN spider_name VARCHAR(200) NULL"
            with engine.begin() as conn:
                if dialect == "sqlite":
                    conn.execute(text(f"ALTER TABLE sources {stmt}"))
                else:
                    conn.execute(text(f"ALTER TABLE sources {stmt}"))
        elif dialect == "mysql":
            with engine.begin() as conn:
                conn.execute(
                    text("ALTER TABLE sources MODIFY COLUMN spider_name VARCHAR(200) NULL")
                )
        if dialect == "mysql":
            with engine.begin() as conn:
                conn.execute(
                    text(
                        "ALTER TABLE sources MODIFY COLUMN type VARCHAR(500) NOT NULL "
                        "COMMENT '类型标签，英文逗号分隔'"
                    )
                )

    if "tasks" in insp.get_table_names():
        cols = {c["name"] for c in insp.get_columns("tasks")}
        if "runtime_config" not in cols:
            stmt = "ADD COLUMN runtime_config TEXT NULL"
            with engine.begin() as conn:
                if dialect == "sqlite":
                    conn.execute(text(f"ALTER TABLE tasks {stmt}"))
                else:
                    conn.execute(text(f"ALTER TABLE tasks {stmt}"))

    if "task_executions" in insp.get_table_names():
        cols = {c["name"] for c in insp.get_columns("task_executions")}
        if "attempt_count" not in cols:
            stmt = "ADD COLUMN attempt_count INTEGER DEFAULT 1"
            with engine.begin() as conn:
                conn.execute(text(f"ALTER TABLE task_executions {stmt}"))
        if dialect == "mysql" and "spider_name" in cols:
            with engine.begin() as conn:
                conn.execute(
                    text(
                        "ALTER TABLE task_executions MODIFY COLUMN spider_name VARCHAR(200) NULL"
                    )
                )


def seed_default_sources(engine: Engine) -> None:
    """若 sources 表为空，插入与爬虫 item 中 source_id 对齐的默认行（1–4）。"""
    from src.models.sentiment import Source

    Session = sessionmaker(bind=engine)
    db = Session()
    try:
        if db.query(Source).count() > 0:
            return
        rows = [
            Source(
                id=1,
                name="bilibili_hot",
                spider_name="bilibili_hot",
                url="https://www.bilibili.com",
                type="social_media",
                category="视频",
                enabled=1,
            ),
            Source(
                id=2,
                name="today_hot",
                spider_name="today_hot",
                url="https://tophub.today",
                type="news",
                category="聚合",
                enabled=1,
            ),
            Source(
                id=3,
                name="weibo_hot",
                spider_name="weibo_hot",
                url="https://weibo.com",
                type="social_media",
                category="社交",
                enabled=1,
            ),
            Source(
                id=4,
                name="zhihu_hot",
                spider_name="zhihu_hot",
                url="https://www.zhihu.com",
                type="forum",
                category="问答",
                enabled=1,
            ),
        ]
        db.add_all(rows)
        db.commit()
    finally:
        db.close()
