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
package icu.jiapeng.kitty.user.api.cfg.dto;

import icu.jiapeng.kitty.common.core.page.PageReqDTO;
import icu.jiapeng.kitty.user.api.cfg.vo.ConfigClassListVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配置分类分页查询参数
 *
 * @author jiapeng
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "配置分类分页查询参数")
public class ConfigClassPageDTO extends PageReqDTO<ConfigClassListVo> {

    @Schema(description = "关键词", nullable = true)
    private String searchKey;

    @Schema(description = "分类所有者", nullable = true)
    private String owner;
}
