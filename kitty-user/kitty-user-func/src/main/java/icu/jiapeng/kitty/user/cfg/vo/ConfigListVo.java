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
package icu.jiapeng.kitty.user.cfg.vo;


import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 *
 *
 * @author jiapeng
 * @since 2025/12/20
 */
@Data
@Schema(description = "配置项分页结果")
public class ConfigListVo {
    @Schema(description = "配置项ID")
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
    @Schema(description = "修改时间")
    private Date updateTime;
    /**
     * 修改者
     */
    @Schema(description = "修改者")
    private String updater;


    /**
     * 配置项名称
     */
    @Schema(description = "配置项名称")
    private String configName;

    /**
     * 配置项 KEY
     */
    @TableField("config_key")
    @NotNull
    private String configKey;

    /**
     * 配置项详细描述
     */
    @TableField("config_desc")
    private String configDesc;

    /**
     * 配置方式
     * {@link icu.jiapeng.kitty.user.cfg.constants.ConfigWay}
     *
     */
    @TableField("config_way")
    @NotNull
    private String configWay;

    /**
     * 配置项枚举值configWay=RADIO/CHECKBOX有效
     * {@link java.util.Map} Map<String,String> 枚举值-枚举label
     */
    @TableField("config_enum")
    private Map<String,String> configEnum;

    /**
     * 所属分类 id
     */
    @TableField("class_id")
    private String classId;
}
