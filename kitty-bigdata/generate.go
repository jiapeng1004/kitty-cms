// Package main 的 go generate 入口。
// 在项目根目录执行：
//
//	go generate
package main

//go:generate go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
//go:generate go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest

// 生成 api/proto 下的 proto 到 internal/grpcpb。
// 在 generate 时会要求本模块的 module path 可用，因此务必先完成 go mod tidy。
//

//go:generate protoc -I api/proto --go_out=. --go_opt=module=kitty-topic --go-grpc_out=. --go-grpc_opt=module=kitty-topic api/proto/kitty_user_auth.proto
