"""
运行爬虫脚本
"""

import sys
import os

# 添加src目录到路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "src"))

from src.services.crawler import CrawlerService


async def main():
    """主函数"""
    crawler = CrawlerService()
    
    # 示例：爬取单个URL
    result = crawler.crawl_single_url("https://example.com")
    print(result)


if __name__ == "__main__":
    import asyncio
    asyncio.run(main())
