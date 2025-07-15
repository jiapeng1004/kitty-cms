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
package icu.jiapeng.kitty.user.cfg.constants;

import icu.jiapeng.kitty.common.core.valid.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 配置方式枚举
 *
 * @author jiapeng
 * @since 2025/12/20
 */
@AllArgsConstructor
@Getter
public enum ConfigWay implements ArrayValuable<String> {
    /**
     * 文本框
     */
    TEXT("TEXT", "文本框"),

    /**
     * 长文本域
     */
    TEXTAREA("TEXTAREA", "长文本域"),

    /**
     * 单选框
     */
    RADIO("RADIO", "单选框"),

    /**
     * 多选框
     */
    CHECKBOX("CHECKBOX", "多选框"),
    ;

    /**
     * 配置方式
     */
    private final String way;

    /**
     * 描述
     */
    private final String desc;

    @Override
    public String[] array() {
        return Arrays.stream(values()).map(ConfigWay::getWay).toArray(String[]::new);
    }
}
