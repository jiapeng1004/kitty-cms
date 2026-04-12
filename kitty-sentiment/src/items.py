# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

import scrapy


class SentimentItem(scrapy.Item):
    """舆情数据项"""
    title = scrapy.Field()
    url = scrapy.Field()
    content = scrapy.Field()
    author = scrapy.Field()
    publish_time = scrapy.Field()
    sentiment_score = scrapy.Field()
    sentiment_label = scrapy.Field()
    keywords = scrapy.Field()
    views = scrapy.Field()
    likes = scrapy.Field()
    comments = scrapy.Field()
    source_type = scrapy.Field()
    source_id = scrapy.Field()
    created_at = scrapy.Field()
    updated_at = scrapy.Field()


class SourceItem(scrapy.Item):
    """信息源项"""
    name = scrapy.Field()
    url = scrapy.Field()
    type = scrapy.Field()
    category = scrapy.Field()
    enabled = scrapy.Field()
    config = scrapy.Field()