FROM alpine:latest

RUN apk --no-cache add ca-certificates

WORKDIR /app

COPY --chmod=0755 kitty-data .
COPY config.yaml .

EXPOSE 9704

CMD ["./kitty-data"]
