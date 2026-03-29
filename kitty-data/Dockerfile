FROM golang:1.22-alpine AS builder

# 安装upx
RUN apk add upx

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -ldflags "-X main.Version=1.0.0 -s -w" -o kitty-data .

RUN upx kitty-data

FROM alpine:latest

RUN apk --no-cache add ca-certificates

WORKDIR /app

COPY --from=builder /app/kitty-data .
COPY --from=builder /app/config.yaml .

EXPOSE 9703

CMD ["./kitty-data"]
