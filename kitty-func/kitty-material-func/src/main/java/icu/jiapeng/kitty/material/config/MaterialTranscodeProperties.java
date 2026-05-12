package icu.jiapeng.kitty.material.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 素材侧转码默认值；可被配置中心 {@code material.transcode.*} 覆盖（见 {@link icu.jiapeng.kitty.material.task.service.MaterialResourceTaskService}）。
 */
@Data
@ConfigurationProperties(prefix = "kitty.material.transcode")
public class MaterialTranscodeProperties {

    /**
     * 物理文件绑定完成后是否自动向 kitty-transcoder（gRPC）下发转码任务。
     * 配置中心键：{@code material.transcode.auto-after-bind}
     */
    private boolean autoAfterBind = true;

    /**
     * kitty-transcoder HTTP 根地址（含 context-path），用于魔法接口同步抽帧/图转。
     * 示例：{@code http://127.0.0.1:9703/kitty-transcoder}。留空则不调魔法 HTTP。
     * 配置中心（material）：{@code transcode.transcoder-http-base}
     */
    private String transcoderHttpBase = "";

    /**
     * 转码 HTTP 认证：登录接口返回的 Token，请求头 {@code Authorization: Bearer ...}。
     * 与 {@link #transcoderAccessKeyId} / {@link #transcoderSecretKey} 二选一；优先使用本字段。
     * 配置中心（material）：{@code transcode.transcoder-api-token}
     */
    private String transcoderApiToken = "";

    /**
     * 转码 HTTP 认证：AK，与 {@link #transcoderSecretKey} 同时配置时使用 AK/SK 签名（与控制台 API 一致）。
     * 配置中心（material）：{@code transcode.transcoder-access-key-id}
     */
    private String transcoderAccessKeyId = "";

    /**
     * 转码 HTTP 认证：SK，与 {@link #transcoderAccessKeyId} 配对。
     * 配置中心（material）：{@code transcode.transcoder-secret-key}
     */
    private String transcoderSecretKey = "";

    /**
     * HTTP 输入时源文件 URL 前缀，与 {@code kt_meta_file.object_key} 拼接后作为转码入参。
     * 需为转码服务可访问的地址（如对象存储对外 HTTP、或内网网关）。
     * 配置中心键：{@code material.transcode.http-input-base}
     */
    private String httpInputBase = "";
}
