# -*- coding: utf-8 -*-
import json
from typing import Any

import scrapy
from datetime import datetime

from scrapy.http import Response

from src.items import SentimentItem


class BilibiliHotSpider(scrapy.Spider):
    """Bilibili热榜爬虫"""
    name = 'bilibili_hot'
    allowed_domains = ['bilibili.com']
    start_urls = ['https://api.bilibili.com/x/web-interface/ranking/v2?rid=0&type=all']

    def _parse(self, response: Response, **kwargs: Any) -> Any:
        """解析Bilibili热榜页面"""
        # 查找热榜列表
        json.loads(str(response.body))
        hot_list = response.xpath('//div[@class="rank-list-wrap"]/ul/li')
        for item in hot_list:
            try:
                title = item.xpath('.//div[@class="info"]/a/text()').get()
                url = item.xpath('.//div[@class="info"]/a/@href').get()
                play = item.xpath('.//div[@class="detail"]/span[1]/text()').get()
                comment = item.xpath('.//div[@class="detail"]/span[2]/text()').get()

                if title and url:
                    yield SentimentItem(
                        title=title.strip(),
                        url=response.urljoin(url),
                        content='',
                        publish_time=datetime.now(),
                        sentiment_score=0.0,
                        sentiment_label='neutral',
                        keywords=title.strip(),
                        views=int(play.replace('播放', '').replace('万', '0000')) if play else 0,
                        comments=int(comment.replace('评论', '')) if comment else 0,
                        source_type='video',
                        source_id=1
                    )

            except Exception as e:
                self.logger.error(f"Failed to parse item: {str(e)}")
                continue
