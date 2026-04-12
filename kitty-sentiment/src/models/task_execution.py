"""
任务执行历史模型
"""

from datetime import datetime

from sqlalchemy import Column, DateTime, ForeignKey, Integer, String, Text

from src.models.base import Base


class TaskExecution(Base):
    """任务执行历史（一次触发对应一条记录）"""

    __tablename__ = "task_executions"

    id = Column(Integer, primary_key=True, index=True)
    task_id = Column(Integer, ForeignKey("tasks.id"), nullable=False, index=True, comment="任务ID")
    trigger_mode = Column(String(32), default="manual", comment="触发方式：manual/schedule/api")
    status = Column(
        String(20),
        default="queued",
        comment="执行状态：queued,running,completed,failed",
    )
    spider_name = Column(String(200), nullable=True, comment="本次执行的 Spider 名称（与信息源名称一致）")
    task_type = Column(String(50), nullable=True, comment="任务类型快照")
    source_id = Column(Integer, nullable=True, comment="信息源ID快照")
    source_name = Column(String(200), nullable=True, comment="信息源名称快照")
    started_at = Column(DateTime, default=datetime.now, comment="开始时间")
    finished_at = Column(DateTime, nullable=True, comment="结束时间")
    items_count = Column(Integer, default=0, comment="抓取条数")
    request_count = Column(Integer, default=0, comment="请求数")
    attempt_count = Column(Integer, default=1, comment="执行尝试次数")
    error_message = Column(Text, nullable=True, comment="失败信息")
    error_trace = Column(Text, nullable=True, comment="异常堆栈（可选）")
    created_at = Column(DateTime, default=datetime.now, comment="创建时间")
