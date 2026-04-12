# Docker使用指南

## 快速开始

### 构建镜像

```bash
# 构建Docker镜像（包含前端和后端）
docker build -t kitty-sentiment:latest .
```

### 运行容器

```bash
# 单容器运行（包含前后端）
docker run -p 8000:8000 --name kitty-sentiment kitty-sentiment:latest

# 查看日志
docker logs -f kitty-sentiment

# 停止服务
docker stop kitty-sentiment

# 删除容器
docker rm kitty-sentiment
```

### 环境配置

```bash
# 复制环境变量示例
cp .env.example .env

# 编辑.env填入你的配置
vi .env

# 重新启动服务
docker run -p 8000:8000 --name kitty-sentiment kitty-sentiment:latest
```

## 镜像特性

### 多阶段构建

Dockerfile使用多阶段构建，确保镜像轻量且安全：

1. **前端构建阶段**：使用Node.js构建React应用
2. **后端构建阶段**：安装Python依赖
3. **生产阶段**：最小化运行时环境

### 镜像大小

| 组件 | 大小 | 说明 |
|------|------|------|
| 基础镜像 | ~150MB | python:3.12-slim |
| 前端构建 | ~200MB | Node.js构建产物 |
| playwright | ~170MB | Chromium浏览器 |
| **总计** | ~520MB | 包含所有依赖 |

### 优化特性

- ✅ 多阶段构建（减小最终镜像）
- ✅ 使用slim基础镜像
- ✅ 无缓存pip安装
- ✅ 清理apt缓存
- ✅ 非root用户运行
- ✅ playwright自动管理浏览器
- ✅ 前端静态文件内嵌

## playwright浏览器管理

crawl4AI使用playwright作为浏览器驱动，playwright会自动管理浏览器：

### 无头模式（默认）
```python
from crawl4ai import WebCrawler

crawler = WebCrawler()
crawler.warmup()

# 无头模式自动启用
result = crawler.run(url="https://example.com")
```

### 有头模式
```python
from crawl4ai import WebCrawler

crawler = WebCrawler()
crawler.warmup()

# 启用有头模式
result = crawler.run(
    url="https://example.com",
    headless=False  # 显示浏览器窗口
)
```

## 环境变量

```bash
# Playwright浏览器路径
PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

# Python环境
PYTHONDONTWRITEBYTECODE=1
PYTHONUNBUFFERED=1

# Node环境
NODE_ENV=production
```

## 前端静态文件

前端React应用已内嵌到Docker镜像中，通过Flask提供服务：

- 前端构建产物路径：`/app/frontend/dist`
- 访问地址：`http://localhost:8000/`

## 调试

```bash
# 进入容器
docker exec -it kitty-sentiment bash

# 查看日志
docker logs -f kitty-sentiment

# 重新初始化数据库
docker exec -it kitty-sentiment python scripts/init_db.py

# 重新构建前端
docker exec -it kitty-sentiment npm run build
```

## 部署到生产环境

```bash
# 构建生产镜像
docker build -t your-registry/kitty-sentiment:prod .

# 推送镜像
docker push your-registry/kitty-sentiment:prod

# 在生产服务器运行
docker run -p 8000:8000 --name kitty-sentiment your-registry/kitty-sentiment:prod
```

## Kubernetes部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kitty-sentiment
spec:
  replicas: 2
  selector:
    matchLabels:
      app: kitty-sentiment
  template:
    metadata:
      labels:
        app: kitty-sentiment
    spec:
      containers:
      - name: app
        image: your-registry/kitty-sentiment:prod
        ports:
        - containerPort: 8000
        env:
        - name: DATABASE_TYPE
          value: "mysql"
        - name: DATABASE_HOST
          value: "mysql-service"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /
            port: 8000
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /
            port: 8000
          initialDelaySeconds: 5
          periodSeconds: 5
```

## robots.txt合规性

本系统严格遵守robots.txt协议，所有爬虫在访问网站前都会检查：

1. 自动读取目标网站的 `robots.txt` 文件
2. 检查当前User-Agent是否被允许访问目标URL
3. 如果robots.txt禁止访问，爬虫将拒绝执行

**注意**：请确保您的使用符合目标网站的robots.txt协议，避免法律风险。

## 常见问题

### 1. playwright浏览器安装失败

```bash
# 手动安装playwright浏览器
playwright install chromium --with-deps
```

### 2. 内存不足

```bash
# 增加容器内存限制
docker run --memory=1g -p 8000:8000 kitty-sentiment:latest
```

### 3. 网络问题

```bash
# 使用host网络
docker run --network=host kitty-sentiment:latest
```

### 4. 权限问题

```bash
# 确保容器以非root用户运行
USER appuser
```

### 5. 前端无法访问

```bash
# 检查前端构建产物是否存在
ls -la /app/frontend/dist

# 重新构建前端
npm run build
```

### 6. robots.txt检查失败

```bash
# 查看爬虫日志
docker logs -f kitty-sentiment | grep robots

# 如果网站robots.txt不允许访问，请联系网站管理员
```
