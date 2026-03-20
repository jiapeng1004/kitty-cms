package icu.jiapeng.kitty.clickqk.entity;


import icu.jiapeng.kitty.clickqk.constants.OperateType;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.bson.codecs.pojo.annotations.BsonId;

/**
 *
 *
 * @author jiapeng
 * @since 2026/3/21
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldNameConstants
public class Operate {
    @BsonId
    private String id;

    /**
     * 哪个客户端上报的
     */
    @Nonnull
    private String ak;

    @Nonnull
    private OperateType operateType;
    /**
     * 操作者可空
     */
    private String operator;
    private Long value;

    /**
     * 时间戳
     */
    private Long timestamp;
}
