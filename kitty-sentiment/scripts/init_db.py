"""
数据库初始化脚本
"""

import sys
import os

# 添加项目根目录到路径
project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, project_root)

from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker

from src.config import settings
from src.models.base import Base
from src.models.sentiment import Sentiment, Source
from src.models.task import Task


def init_database():
    """初始化数据库"""
    # 创建引擎
    engine = create_engine(
        settings.database.connection_url,
        echo=True
    )
    
    # 创建所有表
    Base.metadata.create_all(engine)
    
    print("数据库初始化成功!")
    print(f"数据库类型: {settings.database.type}")
    print(f"数据库名称: {settings.database.name}")
    
    # 创建会话
    Session = sessionmaker(bind=engine)
    session = Session()
    
    # 检查表是否创建成功
    if settings.database.type == "sqlite":
        result = session.execute(text("SELECT name FROM sqlite_master WHERE type='table'"))
        tables = [row[0] for row in result]
    else:
        result = session.execute(text("SHOW TABLES"))
        tables = [row[0] for row in result]
    
    print(f"创建的表: {tables}")
    
    session.close()


if __name__ == "__main__":
    init_database()
