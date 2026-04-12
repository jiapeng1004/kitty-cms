# -*- coding: utf-8 -*-
import scrapy
from datetime import datetime
from src.items import SentimentItem


class TodayHotSpider(scrapy.Spider):
    """今日热榜爬虫"""
    name = 'today_hot'
    allowed_domains = ['tophub.today']
    start_urls = ['https://tophub.today/']
    
    def parse(self, response):
        """解析今日热榜页面"""
        
        # 查找热榜列表
        hot_list = response.xpath('//div[@class="page-content"]//div[@class="list"]/div')
        
        for item in hot_list:
            try:
                title = item.xpath('.//a/text()').get()
                url = item.xpath('.//a/@href').get()
                
                if title and url:
                    yield SentimentItem(
                        title=title.strip(),
                        url=response.urljoin(url),
                        content='',
                        publish_time=datetime.now(),
                        sentiment_score=0.0,
                        sentiment_label='neutral',
                        keywords=title.strip(),
                        source_type='news',
                        source_id=2
                    )
                    
            except Exception as e:
                self.logger.error(f"Failed to parse item: {str(e)}")
                continue