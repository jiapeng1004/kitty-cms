# 舆情爬虫项目提案 - kitty-sentiment

## 项目概述

kitty-sentiment是一个基于Python的舆情监控爬虫系统，旨在高效、智能地抓取和分析网络舆情信息。

## 技术栈

### 核心组件
- **爬虫框架**: crawl4AI (最新版本)
  - 智能网页抓取
  - JavaScript渲染支持
  - 自动提取主要内容
  - 无头浏览器优化
  
- **Web框架**: Flask
  - 轻量级REST API服务
  - 简单易用的路由系统
  - 良好的扩展性
  
- **数据验证**: Pydantic
  - 运行时类型检查
  - 数据序列化/反序列化
  - 配置管理
  
- **ORM框架**: SQLAlchemy
  - 支持MySQL和Doris
  - 数据库抽象层
  - 迁移管理

### 辅助工具
- **异步支持**: asyncio + aiohttp
- **日志管理**: logging + loguru
- **配置管理**: python-dotenv
- **测试框架**: pytest

## 项目结构

```
kitty-sentiment/
├── src/
│   ├── __init__.py
│   ├── main.py                 # 应用入口
│   ├── config.py               # 配置管理
│   ├── models/                 # 数据模型
│   │   ├── __init__.py
│   │   ├── base.py             # 基础模型
│   │   ├── sentiment.py        # 舆情模型
│   │   └── source.py           # 信息源模型
│   ├── schemas/                # Pydantic模式
│   │   ├── __init__.py
│   │   ├── sentiment.py        # 舆情数据模式
│   │   └── request.py          # 请求模式
│   ├── services/               # 业务逻辑
│   │   ├── __init__.py
│   │   ├── crawler.py          # 爬虫服务
│   │   ├── analyzer.py         # 分析服务
│   │   └── notifier.py         # 通知服务
│   ├── api/                    # API端点
│   │   ├── __init__.py
│   │   ├── v1/                 # API v1
│   │   │   ├── __init__.py
│   │   │   ├── routes.py       # 路由定义
│   │   │   ├── endpoints/      # 端点实现
│   │   │   │   ├── __init__.py
│   │   │   │   ├── sentiment.py
│   │   │   │   └── sources.py
│   │   │   └── dependencies.py # 依赖注入
│   └── utils/                  # 工具函数
│       ├── __init__.py
│       ├── logger.py
│       └── helpers.py
├── tests/
│   ├── __init__.py
│   ├── conftest.py
│   ├── test_models.py
│   ├── test_services.py
│   └── test_api.py
├── migrations/                 # 数据库迁移
│   ├── versions/
│   └── env.py
├── scripts/                    # 脚本工具
│   ├── init_db.py
│   └── run_crawler.py
├── Dockerfile
├── docker-compose.yml
├── requirements.txt
├── requirements-dev.txt
├── pyproject.toml
├── .env.example
├── .gitignore
└── README.md
```

## 核心功能

### 1. 爬虫抓取
- 支持多种信息源配置
- 智能内容提取
- 自动去重
- 错误重试机制
- 速率限制控制

### 2. 数据存储
- MySQL: 关系型数据存储
- Doris: 分析型数据存储
- 数据分区策略
- 索引优化

### 3. API服务
- RESTful API设计
- OpenAPI/Swagger文档
- 分页查询支持
- 批量操作接口

### 4. 舆情分析
- 情感分析集成
- 关键词提取
- 趋势分析
- 报警通知

## 数据模型

### Sentiment (舆情)
```python
- id: 主键
- source_id: 信息源ID
- title: 标题
- content: 内容
- url: 原文链接
- author: 作者
- publish_time: 发布时间
- sentiment_score: 情感得分
- sentiment_label: 情感标签
- keywords: 关键词
- created_at: 创建时间
- updated_at: 更新时间
```

### Source (信息源)
```python
- id: 主键
- name: 名称
- url: URL
- type: 类型(新闻/论坛/社交媒体)
- category: 分类
- enabled: 是否启用
- config: 配置信息
- created_at: 创建时间
- updated_at: 更新时间
```

## API端点

### 舆情管理
- `POST /api/v1/sentiments` - 创建舆情
- `GET /api/v1/sentiments` - 查询舆情列表
- `GET /api/v1/sentiments/{id}` - 查询舆情详情
- `PUT /api/v1/sentiments/{id}` - 更新舆情
- `DELETE /api/v1/sentiments/{id}` - 删除舆情

### 信息源管理
- `POST /api/v1/sources` - 创建信息源
- `GET /api/v1/sources` - 查询信息源列表
- `GET /api/v1/sources/{id}` - 查询信息源详情
- `PUT /api/v1/sources/{id}` - 更新信息源
- `DELETE /api/v1/sources/{id}` - 删除信息源

### 爬虫操作
- `POST /api/v1/crawl` - 手动触发爬取
- `GET /api/v1/crawl/status` - 爬取状态
- `POST /api/v1/crawl/schedule` - 设置定时任务

## 部署方案

### 本地开发
```bash
# 安装依赖
pip install -r requirements-dev.txt

# 初始化数据库
python scripts/init_db.py

# 运行应用
python src/main.py
```

### Docker部署
```bash
# 构建镜像
docker build -t kitty-sentiment .

# 运行容器
docker-compose up -d
```

## 开发计划

### Phase 1: 基础框架
- [ ] 项目初始化
- [ ] 数据库模型设计
- [ ] API基础架构
- [ ] 配置管理

### Phase 2: 爬虫功能
- [ ] crawl4AI集成
- [ ] 信息源管理
- [ ] 数据抓取
- [ ] 数据存储

### Phase 3: 分析功能
- [ ] 情感分析
- [ ] 关键词提取
- [ ] 趋势分析

### Phase 4: 完善优化
- [ ] 错误处理
- [ ] 日志监控
- [ ] 性能优化
- [ ] 单元测试

## 注意事项

- 当前API全开放，后续将自行实现鉴权
- 支持MySQL和Doris双数据库
- 使用Pydantic进行严格的数据验证
- 遵循RESTful API设计规范
