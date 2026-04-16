package icu.jiapeng.kitty.user.cfg.grpc;


import icu.jiapeng.kitty.user.cfg.dto.GetValDTO;
import icu.jiapeng.kitty.user.cfg.service.KtConfigService;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 *
 *
 * @author jiapeng
 * @since 2025/12/21
 */
@GrpcService
public class KtConfigSvc extends KtConfigSvcGrpc.KtConfigSvcImplBase {
    @Resource
    private KtConfigService ktConfigService;

    @Override
    public void getVal(icu.jiapeng.kitty.user.cfg.grpc.GetValReq request, StreamObserver<icu.jiapeng.kitty.user.cfg.grpc.GetValResp> responseObserver) {
        try {
            icu.jiapeng.kitty.user.cfg.grpc.GetValResp.Builder builder = icu.jiapeng.kitty.user.cfg.grpc.GetValResp.newBuilder();
            builder.setConfigValue(ktConfigService.getVal(new GetValDTO().setConfigKey(request.getConfigKey())));
            responseObserver.onNext(builder.build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}
