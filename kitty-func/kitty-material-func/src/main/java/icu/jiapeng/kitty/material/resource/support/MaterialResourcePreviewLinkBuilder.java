package icu.jiapeng.kitty.material.resource.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 列表/详情中的预览链接：走应用接口（带鉴权），不直接暴露对象存储直链。
 */
@Component
public class MaterialResourcePreviewLinkBuilder {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /**
     * 浏览器可用的相对路径（需拼网关或 material 服务 origin），例如：
     * {@code /kitty-mam/api/material/resource/preview?resourceId=...}
     */
    public String buildRelativePreviewPath(String resourceId) {
        if (resourceId == null || resourceId.isBlank()) {
            return null;
        }
        String cp = contextPath == null ? "" : contextPath.trim();
        if (!cp.startsWith("/")) {
            cp = "/" + cp;
        }
        return cp + "/api/material/resource/preview?resourceId="
                + URLEncoder.encode(resourceId.trim(), StandardCharsets.UTF_8);
    }

    /** 视频关键帧（抽帧产物就绪后由列表赋值；接口见 /keyframe） */
    public String buildRelativeKeyframePath(String resourceId) {
        if (resourceId == null || resourceId.isBlank()) {
            return null;
        }
        String cp = contextPath == null ? "" : contextPath.trim();
        if (!cp.startsWith("/")) {
            cp = "/" + cp;
        }
        return cp + "/api/material/resource/keyframe?resourceId="
                + URLEncoder.encode(resourceId.trim(), StandardCharsets.UTF_8);
    }
}
