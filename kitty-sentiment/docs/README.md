# kitty-sentiment

舆情监控爬虫系统 - Sentiment Monitoring Crawler System

## 功能特性

- 🕷️ 智能网页抓取 (crawl4AI)
- 📊 数据存储 (MySQL + Doris)
- 🎯 情感分析
- 🔄 RESTful API服务
- 📝 数据验证 (Pydantic)
- 🚀 异步处理
- 🔄 数据库迁移

## 技术栈

- **爬虫**: crawl4AI
- **Web**: Flask
- **数据验证**: Pydantic
- **ORM**: SQLAlchemy
- **数据库**: MySQL, Doris
- **异步**: asyncio + aiohttp

## 快速开始

### 安装依赖

```bash
pip install -r requirements.txt
```

### 配置环境

```bash
cp .env.example .env
# 编辑 .env 填入你的配置
```

### 初始化数据库

```bash
python scripts/init_db.py
```

### 运行应用

```bash
python src/main.py
```

## 项目结构

```
kitty-sentiment/
├── src/
│   ├── api/          # API端点
│   ├── config/       # 配置管理
│   ├── models/       # 数据模型
│   ├── schemas/      # Pydantic模式
│   ├── services/     # 业务逻辑
│   ├── utils/        # 工具函数
│   └── main.py       # 应用入口
├── tests/            # 测试
├── migrations/       # 数据库迁移
├── scripts/          # 脚本工具
└── requirements.txt  # 依赖
```

## API文档

启动应用后访问: `http://localhost:8000/docs`

## 开发

```bash
# 安装开发依赖
pip install -r requirements-dev.txt

# 运行测试
pytest

# 代码检查
ruff check src/
mypy src/
```

## Docker部署

```bash
docker build -t kitty-sentiment .
docker-compose up -d
```

## 许可证

MIT License
