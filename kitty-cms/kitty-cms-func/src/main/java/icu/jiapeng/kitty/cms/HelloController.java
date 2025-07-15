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
package icu.jiapeng.kitty.cms;

import icu.jiapeng.kitty.cms.httpclient.RuntimeInfoApi;
import icu.jiapeng.kitty.cms.runtime.vo.RuntimeInfo;
import icu.jiapeng.kitty.user.auth.grpc.AuthServiceGrpc;
import icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoGetReq;
import icu.jiapeng.kitty.user.auth.grpc.SaTokenDaoGetResp;
import jakarta.annotation.Resource;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequestMapping("/api/hello")
@RestController
public class HelloController {
    @Resource
    @Lazy
    private RuntimeInfoApi localRuntimeApi;

    @GrpcClient("kitty-user")
    private AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub;

    @RequestMapping("/world")
    public RuntimeInfo helloWorld() {
        SaTokenDaoGetResp saTokenDaoGetResp = authServiceBlockingStub.saTokenDaoGet(SaTokenDaoGetReq.newBuilder().setKey("aaaa").buildPartial());
        return localRuntimeApi.getRuntimeInfo();
    }
}
