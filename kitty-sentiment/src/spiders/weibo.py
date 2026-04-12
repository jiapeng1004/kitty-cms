# -*- coding: utf-8 -*-
import scrapy
from datetime import datetime
from src.items import SentimentItem


class WeiboHotSpider(scrapy.Spider):
    """微博热榜爬虫"""
    name = 'weibo_hot'
    allowed_domains = ['weibo.com']
    start_urls = ['https://s.weibo.com/top/summary?cate=realtimehot']
    
    def parse(self, response):
        """解析微博热榜页面"""
        
        # 查找热榜列表
        hot_list = response.xpath('//table[@class="data"]/tbody/tr')
        
        for item in hot_list:
            try:
                title = item.xpath('.//td[@class="td-02"]/a/text()').get()
                url = item.xpath('.//td[@class="td-02"]/a/@href').get()
                
                if title and url:
                    yield SentimentItem(
                        title=title.strip(),
                        url=response.urljoin(url),
                        content='',
                        publish_time=datetime.now(),
                        sentiment_score=0.0,
                        sentiment_label='neutral',
                        keywords=title.strip(),
                        source_type='social',
                        source_id=3
                    )
                    
            except Exception as e:
                self.logger.error(f"Failed to parse item: {str(e)}")
                continue