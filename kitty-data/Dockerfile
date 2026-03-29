FROM alpine:latest

RUN apk --no-cache add ca-certificates

WORKDIR /app

COPY kitty-data .

EXPOSE 9704

CMD ["./kitty-data"]
