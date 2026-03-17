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
package icu.jiapeng.kitty.user.role.grpc;


import icu.jiapeng.kitty.user.role.service.KtUserRoleService;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;

/**
 * ktRole svc
 *
 * @author jiapeng
 * @since 2025/12/19
 */
@GrpcService
public class KtRoleService extends KtRoleServiceGrpc.KtRoleServiceImplBase {
    @Resource
    private KtUserRoleService ktUserRoleService;

    @Override
    public void getRoleIdsByUserId(UserIdReq request, StreamObserver<RoleCodesResp> responseObserver) {
        try {
            RoleCodesResp.Builder builder = RoleCodesResp.newBuilder();
            List<String> roleIdsByUserId = ktUserRoleService.getRoleIdsByUserId(request.getUserId());
            for (int i = 0; i < roleIdsByUserId.size(); i++) {
                builder.setRoleCodes(i, roleIdsByUserId.get(i));
            }
            responseObserver.onNext(builder.build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}
