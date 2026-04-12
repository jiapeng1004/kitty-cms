"""
舆情数据模型
"""

from datetime import datetime
from sqlalchemy import Column, String, Text, Integer, Float, DateTime, ForeignKey, Index, Enum
from sqlalchemy.orm import relationship
from .base import Base


class Sentiment(Base):
    """舆情数据模型"""
    __tablename__ = "sentiments"

    id = Column(Integer, primary_key=True, autoincrement=True, comment="主键ID")
    source_id = Column(Integer, ForeignKey("sources.id"), nullable=False, comment="信息源ID")
    title = Column(String(500), nullable=False, comment="标题")
    content = Column(Text, nullable=False, comment="内容")
    url = Column(String(1000), nullable=False, comment="原文链接")
    author = Column(String(200), comment="作者")
    publish_time = Column(DateTime, nullable=False, comment="发布时间")
    sentiment_score = Column(Float, default=0.0, comment="情感得分 (-1.0 to 1.0)")
    sentiment_label = Column(String(50), default="neutral", comment="情感标签 (positive/negative/neutral)")
    keywords = Column(String(1000), comment="关键词")
    views = Column(Integer, default=0, comment="浏览量/播放量等")
    likes = Column(Integer, default=0, comment="点赞数")
    comments = Column(Integer, default=0, comment="评论数")
    created_at = Column(DateTime, default=datetime.now, comment="创建时间")
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now, comment="更新时间")

    # 关系
    source = relationship("Source", back_populates="sentiments")

    # 索引
    __table_args__ = (
        Index("idx_source_id", "source_id"),
        Index("idx_publish_time", "publish_time"),
        Index("idx_sentiment_label", "sentiment_label"),
        Index("idx_keywords", "keywords", mysql_length=100),
    )

    def __repr__(self):
        return f"<Sentiment(id={self.id}, title='{self.title[:20]}...')>"


class Source(Base):
    """信息源模型"""
    __tablename__ = "sources"

    id = Column(Integer, primary_key=True, autoincrement=True, comment="主键ID")
    name = Column(String(200), nullable=False, comment="名称")
    spider_name = Column(String(200), nullable=True, comment="与 name 一致，作为 Scrapy spider 名（可中文）")
    url = Column(String(500), nullable=False, comment="URL")
    type = Column(String(500), nullable=False, comment="类型标签，英文逗号分隔")
    category = Column(String(100), comment="分类")
    enabled = Column(Integer, default=1, comment="是否启用 (0=禁用, 1=启用)")
    config = Column(Text, comment="配置信息 (JSON)")
    created_at = Column(DateTime, default=datetime.now, comment="创建时间")
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now, comment="更新时间")

    # 关系
    sentiments = relationship("Sentiment", back_populates="source", cascade="all, delete-orphan")

    def __repr__(self):
        return f"<Source(id={self.id}, name='{self.name}')>"
