// Package main 的 go generate 入口。
// 在项目根目录执行: go generate
//
// 仅生成 proto（不改代理）: go generate -run protoc .
// 仅换代理: go generate -run "env" .
package main

// 设置 Go 代理（国内可加速）；不需要可注释或执行时用 -run protoc 跳过
//go:generate go env -w GOPROXY=https://goproxy.cn,direct

// 安装 proto 插件（首次或升级后执行一次）
//go:generate go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
//go:generate go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest

// 根据 api/proto/transcode.proto 生成 internal/grpcpb 下的 Go 与 gRPC 代码
//go:generate protoc -I api/proto --go_out=. --go_opt=module=kitty-transcoder-go --go-grpc_out=. --go-grpc_opt=module=kitty-transcoder-go api/proto/transcode.proto
