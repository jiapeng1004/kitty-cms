package icu.jiapeng.kitty.clickqk.dto;

import icu.jiapeng.kitty.clickqk.constants.OperateType;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReportReq {
    @Nonnull
    private OperateType operateType;

    /**
     * 操作者可空
     */
    private String operator;

    /**
     * 操作值
     */
    private Long value;

    /**
     * 时间戳
     */
    private Long timestamp;
}