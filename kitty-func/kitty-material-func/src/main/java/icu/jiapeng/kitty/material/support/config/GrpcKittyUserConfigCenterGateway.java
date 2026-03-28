package icu.jiapeng.kitty.material.support.config;

import icu.jiapeng.kitty.material.config.ConfigCenterGateway;
import icu.jiapeng.kitty.user.cfg.grpc.GetValReq;
import icu.jiapeng.kitty.user.cfg.grpc.KtConfigSvcGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 6.4：通过 kitty-user {@link KtConfigSvcGrpc} 读取配置，配置键为 {@code namespace + "." + key}。
 */
@Slf4j
@Component
public class GrpcKittyUserConfigCenterGateway implements ConfigCenterGateway {

    private final KtConfigSvcGrpc.KtConfigSvcBlockingStub ktConfigSvcBlockingStub;

    public GrpcKittyUserConfigCenterGateway(@GrpcClient("kitty-user") KtConfigSvcGrpc.KtConfigSvcBlockingStub ktConfigSvcBlockingStub) {
        this.ktConfigSvcBlockingStub = ktConfigSvcBlockingStub;
    }

    @Override
    public Optional<String> getString(String namespace, String key) {
        if (namespace == null || namespace.isBlank() || key == null || key.isBlank()) {
            return Optional.empty();
        }
        String configKey = namespace.trim() + "." + key.trim();
        try {
            String value = ktConfigSvcBlockingStub
                    .getVal(GetValReq.newBuilder().setConfigKey(configKey).build())
                    .getConfigValue();
            if (value == null || value.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(value);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("config getVal failed key={}", configKey, e);
            }
            return Optional.empty();
        }
    }
}
