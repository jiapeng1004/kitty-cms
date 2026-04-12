# -*- coding: utf-8 -*-
import scrapy
from datetime import datetime
from src.items import SentimentItem


class ZhihuHotSpider(scrapy.Spider):
    """知乎热榜爬虫"""
    name = 'zhihu_hot'
    allowed_domains = ['zhihu.com']
    start_urls = ['https://www.zhihu.com/hot']
    
    def parse(self, response):
        """解析知乎热榜页面"""
        
        # 查找热榜列表
        hot_list = response.xpath('//div[@class="HotList-list"]/div')
        
        for item in hot_list:
            try:
                title = item.xpath('.//div[@class="HotItem-title"]/text()').get()
                url = item.xpath('.//a[@class="HotItem-title"]/@href').get()
                
                if title and url:
                    yield SentimentItem(
                        title=title.strip(),
                        url=response.urljoin(url),
                        content='',
                        publish_time=datetime.now(),
                        sentiment_score=0.0,
                        sentiment_label='neutral',
                        keywords=title.strip(),
                        source_type='discussion',
                        source_id=4
                    )
                    
            except Exception as e:
                self.logger.error(f"Failed to parse item: {str(e)}")
                continue