# kitty-sentiment

舆情监控爬虫系统 - Sentiment Monitoring Crawler System

## 功能特性

- 🕷️ 智能网页抓取 (Scrapy)
- 📊 数据存储 (MySQL + Doris)
- 🎯 情感分析
- 🔄 RESTful API服务
- 📝 数据验证 (Pydantic)
- 🚀 异步处理
- 🔄 数据库迁移
- 🤖 遵守robots.txt协议
- 📋 标准User-Agent标识
- 📈 分布式爬虫支持
- 🛡️ 自动节流与反爬机制

## 技术栈

- **爬虫**: Scrapy
- **Web**: Flask
- **数据验证**: Pydantic
- **ORM**: SQLAlchemy
- **数据库**: MySQL, Doris
- **异步**: asyncio + aiohttp
- **前端**: React + Vite + Ant Design
- **分布式**: Scrapy Cluster

## 快速开始

### 安装依赖

```bash
pip install -e .
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
├── frontend/         # React前端应用
│   ├── src/
│   │   ├── pages/    # 页面组件
│   │   ├── App.tsx   # 主应用组件
│   │   └── main.tsx  # 入口文件
│   └── package.json  # 前端依赖
├── tests/            # 测试
├── migrations/       # 数据库迁移
├── scripts/          # 脚本工具
└── pyproject.toml    # 项目配置
```

## API文档

启动应用后访问: `http://localhost:8000/`

前端界面: `http://localhost:8000/`

## 爬虫合规性说明

### 🤖 robots.txt协议

本系统**严格遵守**robots.txt协议，所有爬虫在访问网站前都会：

1. 自动读取目标网站的 `robots.txt` 文件
2. 检查当前User-Agent是否被允许访问目标URL
3. 如果robots.txt禁止访问，爬虫将**拒绝执行**并返回错误

### 📋 标准User-Agent

所有爬虫使用统一的、可识别的User-Agent：

```
KittySentimentBot/1.0 (+https://github.com/kitty-cms/kitty-sentiment; sentiment-monitoring@kitty.dev)
```

该User-Agent明确标识了：
- 爬虫名称：KittySentimentBot
- 版本号：1.0
- 项目主页：https://github.com/kitty-cms/kitty-sentiment
- 联系邮箱：sentiment-monitoring@kitty.dev

### ⚠️ 高风险操作警告

以下操作**违反**robots.txt协议，存在法律和道德风险，**禁止使用**：

| 高风险操作 | 合规风险 | 建议 |
|-----------|---------|------|
| 伪装User-Agent | 可能违反网站服务条款 | 使用标准User-Agent |
| 使用IP代理池 | 可能被识别为恶意爬虫 | 限制请求频率 |
| 绕过robots.txt | 违反robots.txt协议 | 严格遵守协议 |
| 高频请求 | 可能导致服务器过载 | 设置合理的请求间隔 |
| 破解反爬机制 | 违反计算机安全法规 | 避免接触反爬机制 |

### ✅ 合规使用指南

1. **请求频率限制**：建议每分钟不超过60次请求
2. **错误重试**：遇到429（Too Many Requests）时，等待30秒后重试
3. **robots.txt检查**：所有爬虫必须先检查robots.txt
4. **User-Agent标识**：必须使用标准的、可识别的User-Agent
5. **数据使用**：爬取的数据仅用于个人研究或授权的商业用途

### 📄 网站例外列表

以下网站已确认允许爬虫访问（需检查robots.txt）：

- bilibili热门榜单：https://www.bilibili.com/v/popular/rank/all
- 今日热榜：https://tophub.today/

**注意**：即使这些网站允许爬取，也必须遵守其robots.txt的具体规则。

## 开发

```bash
# 安装开发依赖
pip install -e ".[dev]"

# 运行测试
pytest

# 代码检查
ruff check src/
mypy src/

# 前端开发
cd frontend
npm install
npm run dev
```

## Docker部署

```bash
# 构建镜像
docker build -t kitty-sentiment:latest .

# 运行容器
docker run -p 8000:8000 --name kitty-sentiment kitty-sentiment:latest

# 查看日志
docker logs -f kitty-sentiment

# 停止服务
docker stop kitty-sentiment
```

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request！

## 联系方式

- GitHub: https://github.com/kitty-cms/kitty-sentiment
- Email: sentiment-monitoring@kitty.dev
