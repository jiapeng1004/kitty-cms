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
package icu.jiapeng.kitty.user.api.cfg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 配置分类列表/详情 VO
 *
 * @author jiapeng
 */
@Data
@Schema(description = "配置分类列表/详情")
public class ConfigClassListVo {

    @Schema(description = "id")
    private String id;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "修改时间", nullable = true)
    private Date updateTime;

    @Schema(description = "修改者", nullable = true)
    private String updater;

    @Schema(description = "类名")
    private String className;

    @Schema(description = "类描述", nullable = true)
    private String classDesc;

    @Schema(description = "分类所有者", nullable = true)
    private String owner;
}
