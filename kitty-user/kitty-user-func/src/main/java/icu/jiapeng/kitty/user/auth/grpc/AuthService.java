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
package icu.jiapeng.kitty.user.auth.grpc;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.redis.core.StringRedisTemplate;

@GrpcService
public class AuthService extends AuthServiceGrpc.AuthServiceImplBase {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     *
     */
    public void tokenIntrospection(icu.jiapeng.kitty.user.auth.grpc.TokenIntrospectionReq request,
                                   io.grpc.stub.StreamObserver<icu.jiapeng.kitty.user.auth.grpc.TokenIntrospectionResp> responseObserver) {
        try {
            // 使用sa-token校验一下这个token
            Object loginIdByToken = StpUtil.getLoginIdByToken(request.getToken());
            TokenIntrospectionResp.Builder builder = TokenIntrospectionResp.newBuilder();
            builder.setValid(loginIdByToken != null);
            responseObserver.onNext(builder.build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void saTokenDaoGet(SaTokenDaoGetReq request, StreamObserver<SaTokenDaoGetResp> responseObserver) {
        try {
            String value = stringRedisTemplate.opsForValue().get(request.getKey());
            if (StrUtil.isBlank(value)) {
                value = "";
            }
            SaTokenDaoGetResp.Builder builder = SaTokenDaoGetResp.newBuilder();
            builder.setValue(value);
            responseObserver.onNext(builder.build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void saTokenDaoTimeOut(SaTokenDaoTimeOutReq request, StreamObserver<SaTokenDaoTimeOutResp> responseObserver) {
        try {
            long timeout = stringRedisTemplate.getExpire(request.getKey());
            SaTokenDaoTimeOutResp.Builder builder = SaTokenDaoTimeOutResp.newBuilder();
            builder.setTimeOut(timeout);
            responseObserver.onNext(builder.build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}