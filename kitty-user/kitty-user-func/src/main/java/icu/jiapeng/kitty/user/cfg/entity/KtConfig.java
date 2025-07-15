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
package icu.jiapeng.kitty.user.cfg.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 配置项
 *
 * @author jiapeng
 * @since 2025/12/20
 */
@Getter
@Setter
@TableName("kt_config")
public class KtConfig extends CommonEntity {

    /**
     * 配置项名称
     */
    @TableField("config_name")
    @NotNull
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
    private String configEnum;

    /**
     * 配置项值
     */
    @TableField("config_value")
    private String configValue;

    /**
     * 所属分类 id
     */
    @TableField("class_id")
    private String classId;
}
