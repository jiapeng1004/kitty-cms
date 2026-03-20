package icu.jiapeng.kitty.clickqk.entity;


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
 * @since 2026/3/20
 */
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Getter
@Setter
public class AkSk {
    /**
     * app key
     */
    @BsonId
    private String id;
    private String ak;
    private String sk;
    private String name;
}
