package icu.jiapeng.qk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 上报事件入参：与持久化实体分离，避免客户端误传 {@code id} 导致 ObjectId 反序列化失败（400）。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventReportRequest(String eventType, String operator, long value) {
}
