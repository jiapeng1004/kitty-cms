"""
任务管理模型
"""
from sqlalchemy import Column, Integer, String, DateTime, Text
from src.models.base import Base

class Task(Base):
    """爬取任务模型"""
    __tablename__ = "tasks"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False, comment="任务名称")
    type = Column(String(50), nullable=False, comment="任务类型：hotlist, sentiment")
    source_id = Column(Integer, nullable=False, comment="信息源ID")
    source_name = Column(String(100), nullable=False, comment="信息源名称")
    status = Column(String(20), default="stopped", comment="任务状态：running, stopped")
    last_run = Column(DateTime, comment="最后执行时间")
    next_run = Column(DateTime, comment="下次执行时间")
    frequency = Column(
        String(120),
        default="manual",
        comment="Cron 5段(分 时 日 月 周) 或 manual（仅手动）",
    )
    runtime_config = Column(
        Text,
        nullable=True,
        comment="任务级运行参数(JSON)，如 ROBOTSTXT_OBEY/COOKIES_ENABLED 等",
    )
