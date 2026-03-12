FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/golang:1.25-alpine3.22 as builder

# sed换成阿里云的软件源
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
# 拷贝go.mod cache一下依赖
COPY go.mod go.sum ./
RUN go env -w GOPROXY=https://goproxy.cn,direct && go mod download

# 拷贝源码并编译
COPY . .
RUN go build -o /app

FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/alpine:3.22 as runtime

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
# 安装依赖
RUN apk add --no-cache tzdata imagemagick ffmpeg

WORKDIR /runtime

COPY --from=builder /app ./app

COPY dist ./dist

# 前端静态资源目录，供 Go 静态处理器使用（不设则不启用静态服务）
ENV STATIC_DIR=/runtime/dist

ENTRYPOINT ["/runtime/app"]
