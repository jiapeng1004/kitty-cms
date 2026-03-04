FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/openjdk:25-jdk-slim

LABEL maintainer="jiapeng_aoa@163.com"
LABEL version="1.0"
LABEL description="kitty-transcoder"

COPY kitty-transcoder-server/target/app.jar /app/app.jar

# 默认端口
EXPOSE 9703 9803
#复制jar包到工作目录
WORKDIR /app


ENTRYPOINT ["java"]

CMD ["-jar", "app.jar"]
