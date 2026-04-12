"""
模型测试用例
"""

import pytest
from datetime import datetime
from src.models.sentiment import Sentiment, Source
from src.models.base import Base
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker


@pytest.fixture
def session():
    """创建测试数据库会话"""
    engine = create_engine("sqlite:///:memory:")
    Base.metadata.create_all(engine)
    Session = sessionmaker(bind=engine)
    session = Session()
    yield session
    session.close()
    Base.metadata.drop_all(engine)


def test_create_source(session):
    """测试创建信息源"""
    source = Source(
        name="测试源",
        url="https://example.com",
        type="news",
        category="测试"
    )
    session.add(source)
    session.commit()
    
    assert source.id is not None
    assert source.name == "测试源"
    assert source.url == "https://example.com"
    assert source.type == "news"
    assert source.category == "测试"
    assert source.enabled == 1


def test_create_sentiment(session):
    """测试创建舆情数据"""
    source = Source(
        name="测试源",
        url="https://example.com",
        type="news"
    )
    session.add(source)
    session.commit()
    
    sentiment = Sentiment(
        source_id=source.id,
        title="测试标题",
        content="测试内容",
        url="https://example.com/test",
        publish_time=datetime.now(),
        sentiment_score=0.5,
        sentiment_label="positive",
        keywords="测试, 关键词"
    )
    session.add(sentiment)
    session.commit()
    
    assert sentiment.id is not None
    assert sentiment.title == "测试标题"
    assert sentiment.content == "测试内容"
    assert sentiment.url == "https://example.com/test"
    assert sentiment.sentiment_score == 0.5
    assert sentiment.sentiment_label == "positive"
    assert sentiment.keywords == "测试, 关键词"


def test_sentiment_relationship(session):
    """测试舆情与信息源的关系"""
    source = Source(
        name="测试源",
        url="https://example.com",
        type="news"
    )
    session.add(source)
    session.commit()
    
    sentiment = Sentiment(
        source_id=source.id,
        title="测试标题",
        content="测试内容",
        url="https://example.com/test",
        publish_time=datetime.now()
    )
    session.add(sentiment)
    session.commit()
    
    assert sentiment.source == source
    assert len(source.sentiments) == 1
    assert source.sentiments[0] == sentiment


if __name__ == "__main__":
    pytest.main(["-v", __file__])