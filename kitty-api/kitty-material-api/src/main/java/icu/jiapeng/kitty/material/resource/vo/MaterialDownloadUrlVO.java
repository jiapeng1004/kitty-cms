package icu.jiapeng.kitty.material.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "下载/拉取用直链（与存储/转码机可达性一致，未必为带过期签名的 S3 预签）")
@Data
public class MaterialDownloadUrlVO {

    @Schema(description = "资源 id")
    private String resourceId;

    @Schema(description = "请求的分级")
    private String destinationType;

    @Schema(description = "实际命中的分级（例如批量选中 720P 时降级为 SOURCE 则在此区分）")
    private String actualDestinationType;

    @Schema(description = "可 GET 的 URL，客户端自行带 Cookie/鉴权头访问同源网关")
    private String url;

    @Schema(description = "建议缓存秒数/客户端提示；无强 TTL 时固定 3600")
    private int expiresInSec = 3600;
}
