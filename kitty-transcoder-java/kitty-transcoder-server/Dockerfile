FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/bellsoft/liberica-openjdk-alpine-musl:25

LABEL maintainer="jiapeng_aoa@163.com"
LABEL version="1.0"
LABEL description="kitty-transcoder"

RUN <<EOF
# 换清华源
sed -i 's/dl-cdn.alpinelinux.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apk/repositories
# 安装gcompat兼容层
apk add gcompat
# 安装ffmpeg
apk add ffmpeg
# 安装image magick
apk add --no-cache ffmpeg imagemagick imagemagick-heic imagemagick-webp imagemagick-jpeg
EOF

# 后端 jar
COPY kitty-transcoder/kitty-transcoder-server/target/app.jar /app/app.jar

# 前端静态资源（由 CI 并行构建后传入）
COPY frontend/packages/transcoder/dist /app/static

# 默认端口
EXPOSE 9703 9803
WORKDIR /app

# 配置 Spring Boot 同时从 /app/static 提供前端静态文件
ENV SPRING_WEB_RESOURCES_STATIC_LOCATIONS=file:/app/static/,classpath:/META-INF/resources/,classpath:/resources/,classpath:/static/,classpath:/public/

ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]
