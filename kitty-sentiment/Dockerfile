# 多阶段构建Dockerfile
# 包含后端Flask应用和前端React应用

# ============================================
# Stage 1: Build Frontend (React)
# ============================================
FROM node:20-alpine AS frontend-builder

WORKDIR /build/frontend

# 复制前端文件
COPY frontend/package*.json ./
RUN npm ci --only=production

COPY frontend/ ./
RUN npm run build

# ============================================
# Stage 2: Build Backend (Python)
# ============================================
FROM python:3.12-alpine AS backend-builder

WORKDIR /build

# 安装系统依赖
RUN apk add --no-cache \
    gcc \
    musl-dev \
    libffi-dev \
    openssl-dev \
    python3-dev

# 复制项目文件
COPY pyproject.toml ./
COPY src/ src/

# 安装依赖
RUN pip install --no-cache-dir --user .

# ============================================
# Stage 3: Production
# ============================================
FROM python:3.12-alpine AS production

# 设置环境变量
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PATH=/root/.local/bin:$PATH \
    PLAYWRIGHT_BROWSERS_PATH=/ms-playwright \
    NODE_ENV=production

# 创建非root用户
RUN useradd -m -u 1000 appuser

WORKDIR /app

# 从builder复制已安装的包
COPY --from=backend-builder /root/.local /home/appuser/.local

# 复制后端应用代码
COPY --chown=appuser:appuser src/ src/
COPY --chown=appuser:appuser scripts/ scripts/
COPY --chown=appuser:appuser .env.example .env

# 复制前端构建产物
COPY --from=frontend-builder /build/frontend/dist /app/frontend/dist

# 创建目录
RUN mkdir -p /app/logs /app/data

# 切换到非root用户
USER appuser

# 安装Scrapy依赖
RUN apk add --no-cache \
    libssl3 \
    libffi \
    && rm -rf /var/cache/apk/*

# 暴露端口
EXPOSE 8000

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8000/ || exit 1

# 默认命令
CMD ["python", "-m", "flask", "run", "--host=0.0.0.0", "--port=8000"]
