package icu.jiapeng.kitty.clickqk.dto;

import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

/**
 * /v1/stats/count 查询参数
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldNameConstants
public class StatsCountQuery {

    @QueryParam(Fields.bizType)
    private String bizType;

    @QueryParam(Fields.from)
    private Long from;

    @QueryParam(Fields.to)
    private Long to;
}

