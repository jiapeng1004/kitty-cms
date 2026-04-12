# 构建Docker镜像
docker build -t kitty-sentiment:latest .

# 运行容器
docker run -p 8000:8000 --name kitty-sentiment kitty-sentiment:latest

# 查看日志
docker logs -f kitty-sentiment

# 停止服务
docker stop kitty-sentiment

# 删除容器
docker rm kitty-sentiment

# 构建并推送
docker build -t your-registry/kitty-sentiment:prod .
docker push your-registry/kitty-sentiment:prod

# 本地开发
.PHONY: dev
dev:
	cd frontend && npm install && npm run dev

# 运行测试
.PHONY: test
test:
	pytest

# 代码检查
.PHONY: lint
lint:
	ruff check src/

# 类型检查
.PHONY: typecheck
typecheck:
	mypy src/
