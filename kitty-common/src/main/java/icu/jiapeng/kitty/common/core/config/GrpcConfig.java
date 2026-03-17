/*
 * Copyright [2025] [贾鹏]
 *
 * kitty-cms采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 贾鹏: jiapeng_aoa@163.com。
 * 5.不可二次分发开源参与同类竞品，如有想法可联系 贾鹏: jiapeng_aoa@163.com商议合作。
 */
package icu.jiapeng.kitty.common.core.config;

import io.grpc.*;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.beans.factory.annotation.Value;


public class GrpcConfig {
    @Value("${grpc.server.token:KITTY_CMS}")
    private String serviceBasic;

    @GrpcGlobalServerInterceptor
    public GrpcBasicAuthServerInterceptor grpcBasicAuthServerInterceptor() {
        return new GrpcBasicAuthServerInterceptor(serviceBasic);
    }

    @GrpcGlobalClientInterceptor
    public GrpcBasicAuthClientInterceptor grpcBasicAuthClientInterceptor() {
        return new GrpcBasicAuthClientInterceptor(serviceBasic);
    }

    public record GrpcBasicAuthClientInterceptor(String serviceBasic) implements ClientInterceptor {
        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
            return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    headers.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Basic " + serviceBasic);
                    super.start(responseListener, headers);
                }
            };
        }
    }

    public record GrpcBasicAuthServerInterceptor(String serviceBasic) implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
            // 校验元数据的basic token
            String basic = metadata.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));
            // 放行gRPC反射服务相关方法
            String methodName = serverCall.getMethodDescriptor().getFullMethodName();
            if (methodName.startsWith("grpc.reflection")) {
                return serverCallHandler.startCall(serverCall, metadata);
            }
            if (basic != null && basic.startsWith("Basic ")) {
                // 校验token是否正确
                if (basic.equals(serviceBasic)) {
                    throw Status.UNAUTHENTICATED.withDescription("Auth Failed").asRuntimeException();
                }
            } else {
                throw Status.UNAUTHENTICATED.withDescription("Auth Failed").asRuntimeException();
            }
            return serverCallHandler.startCall(serverCall, metadata);
        }
    }
}
