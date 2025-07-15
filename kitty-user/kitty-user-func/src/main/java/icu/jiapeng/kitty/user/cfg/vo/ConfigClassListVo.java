package icu.jiapeng.kitty.user.cfg.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 *
 *
 * @author jiapeng
 * @since 2025/12/21
 */
@Data
public class ConfigClassListVo {

    @Schema(description = "id")
    private String id;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    /**
     * 创建者
     */
    @Schema(description = "创建者")
    private String creator;
    /**
     * 修改时间
     */
    @Schema(description = "修改时间", nullable = true)
    private Date updateTime;
    /**
     * 修改者
     */
    @Schema(description = "修改者", nullable = true)
    private String updater;
    /**
     * 类名
     */
    @Schema(description = "类名")
    private String className;

    /**
     * 类描述
     */
    @Schema(description = "类描述", nullable = true)
    private String classDesc;

    /**
     * 分类所有者
     * public公共
     */
    @Schema(description = "分类所有者", nullable = true)
    private String owner;
}
