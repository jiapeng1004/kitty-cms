package icu.jiapeng.qk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 上报事件入参：与持久化实体分离，避免客户端误传 id；兼容老客户端仅三字段。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventReportRequest {

    public String eventType;
    public String operator;
    public long value;
    public String detailJson;
}
