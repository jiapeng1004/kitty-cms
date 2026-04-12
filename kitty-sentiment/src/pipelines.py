# -*- coding: utf-8 -*-

# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from datetime import datetime
import logging
import os
from dotenv import load_dotenv

logger = logging.getLogger(__name__)

# 加载环境变量
load_dotenv()


class SentimentPipeline:
    """舆情数据管道"""
    
    def __init__(self, database_url):
        self.database_url = database_url
        self.engine = None
        self.Session = None
        
    @classmethod
    def from_crawler(cls, crawler):
        """从爬虫获取配置"""
        database_url = os.getenv('DATABASE_URL', 'sqlite:///./sentiment.db')
        return cls(
            database_url=database_url
        )
    
    def open_spider(self):
        """爬虫启动时初始化数据库连接（Scrapy 新版不再传入 spider 参数）"""
        self.engine = create_engine(self.database_url)
        self.Session = sessionmaker(bind=self.engine)

    def close_spider(self):
        """爬虫关闭时关闭数据库连接"""
        if self.engine:
            self.engine.dispose()

    def process_item(self, item):
        """处理爬取到的舆情数据"""
        session = self.Session()
        
        try:
            # 导入模型
            from src.models.sentiment import Sentiment
            
            sentiment = Sentiment(
                title=item['title'],
                url=item['url'],
                content=item.get('content', ''),
                author=item.get('author', ''),
                publish_time=item.get('publish_time', datetime.now()),
                sentiment_score=item.get('sentiment_score', 0.0),
                sentiment_label=item.get('sentiment_label', 'neutral'),
                keywords=item.get('keywords', ''),
                views=item.get('views', 0),
                likes=item.get('likes', 0),
                comments=item.get('comments', 0),
                source_id=item.get('source_id', 1),
            )
            
            session.add(sentiment)
            session.commit()
            logger.info(f"Saved sentiment: {item['title']}")
            
        except Exception as e:
            session.rollback()
            logger.error(f"Failed to save sentiment: {str(e)}")
            raise
        finally:
            session.close()
            
        return item