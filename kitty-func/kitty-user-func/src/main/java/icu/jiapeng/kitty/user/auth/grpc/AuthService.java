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

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.annotation.Lazy;

@GrpcService
public class AuthService extends icu.jiapeng.kitty.user.auth.grpc.AuthServiceGrpc.AuthServiceImplBase {

    @Resource
    @Lazy
    private SaTokenDao saTokenDao;

    /**
     *
     */
    public void tokenIntrospection(icu.jiapeng.kitty.user.auth.grpc.TokenIntrospectionReq request,
                                   io.grpc.stub.StreamObserver<icu.jiapeng.kitty.user.auth.grpc.TokenIntrospectionResp> responseObserver) {
        try {
            // 使用sa-token校验一下这个token
            Object loginIdByToken = StpUtil.getLoginIdByToken(request.getToken());
            icu.jiapeng.kitty.user.auth.grpc.TokenIntrospectionResp.Builder builder = icu.jiapeng.kitty.user.auth.grpc.TokenIntrospectionResp.newBuilder();
            builder.setValid(loginIdByToken != null);
            responseObserver.onNext(builder.build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void saTokenDaoGet(icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoGetReq request, StreamObserver<icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoGetResp> responseObserver) {
        try {
            String value = saTokenDao.get(request.getKey());
            if (StrUtil.isBlank(value)) {
                value = "";
            }
            icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoGetResp.Builder builder = icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoGetResp.newBuilder();
            builder.setValue(value);
            responseObserver.onNext(builder.build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void saTokenDaoTimeOut(icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoTimeOutReq request, StreamObserver<icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoTimeOutResp> responseObserver) {
        try {
            long timeout = saTokenDao.getTimeout(request.getKey());
            icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoTimeOutResp.Builder builder = icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoTimeOutResp.newBuilder();
            builder.setTimeOut(timeout);
            responseObserver.onNext(builder.build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void saTokenDaoUpdate(icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoUpdateReq request, StreamObserver<icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoUpdateResp> responseObserver) {
        try {
            String key = request.getKey();
            String value = request.getValue();
            // 直接调用SaTokenDao实现，避免重复造轮子
            saTokenDao.update(key, value);
            icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoUpdateResp.Builder builder = icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoUpdateResp.newBuilder();
            builder.setSuccess(true);
            responseObserver.onNext(builder.build());
        } catch (Exception e) {
            icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoUpdateResp.Builder builder = icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoUpdateResp.newBuilder();
            builder.setSuccess(false);
            responseObserver.onNext(builder.build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}