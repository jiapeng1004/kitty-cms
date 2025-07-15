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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 *
 * @author jiapeng
 * @since 2025/12/20
 */
@Schema(description = "分页结果")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PageRespVo<T> {
    /**
     * 分页大小
     */
    @Schema(description = "分页大小")
    private Long size;
    /**
     * 页码
     */
    @Schema(description = "页码")
    private Long page;

    /**
     * 排序
     */
    @Schema(description = "排序", nullable = true)
    private List<PageReqDTO.OrderItem> orders;

    /**
     * 数据
     */
    @Schema(description = "数据")
    private List<T> records;

    /**
     * 总数
     */
    @Schema(description = "总数")
    private Long total;

    @Schema(description = "总页数")
    public Long getPages() {
        return total / size + (total % size == 0 ? 0 : 1);
    }
}
