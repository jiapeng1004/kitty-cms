"""
爬虫测试用例
"""

import pytest
import asyncio
from src.services.bilibili_crawler import BilibiliHotCrawler
from src.services.today_hot_crawler import TodayHotCrawler
from src.services.weibo_hot_crawler import WeiboHotCrawler
from src.services.zhihu_hot_crawler import ZhihuHotCrawler


@pytest.mark.asyncio
async def test_bilibili_crawler():
    """测试Bilibili热榜爬虫"""
    crawler = BilibiliHotCrawler()
    result = await crawler.crawl(max_pages=1)
    assert result.success == True
    assert result.crawled_count >= 0


@pytest.mark.asyncio
async def test_today_hot_crawler():
    """测试今日热榜爬虫"""
    crawler = TodayHotCrawler()
    result = await crawler.crawl(max_pages=1)
    assert result.success == True
    assert result.crawled_count >= 0


@pytest.mark.asyncio
async def test_weibo_crawler():
    """测试微博热榜爬虫"""
    crawler = WeiboHotCrawler()
    result = await crawler.crawl(max_pages=1)
    assert result.success == True
    assert result.crawled_count >= 0


@pytest.mark.asyncio
async def test_zhihu_crawler():
    """测试知乎热榜爬虫"""
    crawler = ZhihuHotCrawler()
    result = await crawler.crawl(max_pages=1)
    assert result.success == True
    assert result.crawled_count >= 0


if __name__ == "__main__":
    pytest.main(["-v", __file__])