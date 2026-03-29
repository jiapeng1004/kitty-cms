FROM alpine:latest

RUN apk --no-cache add ca-certificates

WORKDIR /app

COPY kitty-data /app/kitty-data
COPY config.yaml /app/config.yaml

EXPOSE 9703

CMD ["./kitty-data"]
