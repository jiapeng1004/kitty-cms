package icu.jiapeng.kitty.user.cfg.dto;


import icu.jiapeng.kitty.common.core.page.PageReqDTO;
import icu.jiapeng.kitty.user.cfg.vo.ConfigListVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 *
 * @author jiapeng
 * @since 2025/12/21
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ConfigClassPageDTO extends PageReqDTO<ConfigListVo> {

    @Schema(description = "关键词", example = "user_email", nullable = true)
    private String searchKey;

    @Schema(description = "分类所有者", example = "public", nullable = true)
    private String owner;
}
