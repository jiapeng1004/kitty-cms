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
package icu.jiapeng.kitty.common.core.page;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 *
 * 分页请求
 *
 * @author jiapeng
 * @since 2025/12/20
 */
public abstract class PageReqDTO<Vo> {
    /**
     * 分页大小
     */
    @Schema(description = "分页大小", defaultValue = "10")
    @Getter
    @Setter
    private long size = 10L;
    /**
     * 页码
     */
    @Schema(description = "页码", defaultValue = "1")
    @Getter
    @Setter
    private long page = 1L;
    /**
     * 排序
     */
    @Schema(description = "排序", nullable = true)
    @Getter
    private List<OrderItem> orders;

    /**
     * 缓存的目标Vo class
     */
    private Class<?> voClass;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        /**
         * 排序字段
         */
        @Schema(description = "排序属性")
        @NotBlank(message = "排序字段不能为空")
        private String orderField;

        /**
         * 排序描述
         * {@link CommonOrder}
         */
        @Schema(description = "排序方式asc/desc")
        @NotBlank(message = "排序描述不能为空")
        private CommonOrder order;
    }


    @AssertFalse
    public boolean illegalOrder() {
        // 获取当前的泛型实参数,检查是不是Vo或者其父类的字段检查order是不是Order的值
        // 没有排序
        if (CollectionUtils.isEmpty(this.orders)) {
            return false;
        }
        if (Objects.isNull(voClass)) {
            voClass = GenericTypeResolver.resolveTypeArgument(this.getClass(), PageReqDTO.class);
        }
        if (Objects.isNull(voClass)) {
            return false;
        }
        // 遍历排序
        for (OrderItem order : this.orders) {
            String fieldName = order.getOrderField();
            // 可以包含逗号
            if (fieldName.contains(",")) {
                for (String aField : fieldName.split(",")) {
                    // 检查是不是Vo 的字段名
                    Field field = ReflectionUtils.findField(voClass, aField.trim());
                    if (Objects.isNull(field)) {
                        return true;
                    }
                }
            } else {
                // 检查是不是Vo 的字段名
                Field field = ReflectionUtils.findField(voClass, fieldName.trim());
                if (Objects.isNull(field)) {
                    return true;
                }
            }
        }
        return false;
    }
}
