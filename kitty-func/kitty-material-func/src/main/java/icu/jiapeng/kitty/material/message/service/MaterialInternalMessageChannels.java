package icu.jiapeng.kitty.material.message.service;

/**
 * 站内信跨 Pod 广播通道名（Redisson {@link org.redisson.api.RTopic}）。
 */
public final class MaterialInternalMessageChannels {

    private MaterialInternalMessageChannels() {
    }

    public static final String BROADCAST_TOPIC = "material:internal-message:broadcast";
}
