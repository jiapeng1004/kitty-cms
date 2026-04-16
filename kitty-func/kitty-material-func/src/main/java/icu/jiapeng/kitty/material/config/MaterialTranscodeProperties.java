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
     * HTTP 输入时源文件 URL 前缀，与 {@code kt_meta_file.object_key} 拼接后作为转码入参。
     * 需为转码服务可访问的地址（如对象存储对外 HTTP、或内网网关）。
     * 配置中心键：{@code material.transcode.http-input-base}
     */
    private String httpInputBase = "";
}
