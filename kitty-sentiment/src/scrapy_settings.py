# Scrapy settings for kitty_sentiment project

import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

BOT_NAME = 'kitty_sentiment'

SPIDER_MODULES = ['src.spiders']
NEWSPIDER_MODULE = 'src.spiders'
# 运行参数（如 ROBOTSTXT_OBEY / COOKIES_ENABLED / USER_AGENT 等）在任务启动时
# 由 src/services/crawler.py 动态注入，按“任务级”配置执行。
