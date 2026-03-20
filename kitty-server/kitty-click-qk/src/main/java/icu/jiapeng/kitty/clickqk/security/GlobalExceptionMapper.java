package icu.jiapeng.kitty.clickqk.security;// src/main/java/org/acme/exception/GlobalExceptionMapper.java

import icu.jiapeng.kitty.clickqk.vo.ErrorResp;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;


@Provider // 注册为 JAX-RS Provider
@Slf4j
public class GlobalExceptionMapper implements ExceptionMapper<Exception> { // 通常处理最通用的 Exception


    @Override
    public Response toResponse(Exception exception) {
        // 记录原始异常，这对于调试至关重要
        log.error("An unhandled exception occurred: ", exception);


        // 处理 Jackson 反序列化错误 (JsonMappingException, JsonParseException)
        if (exception.getCause() instanceof com.fasterxml.jackson.core.JsonProcessingException) {
            ErrorResp failed = ErrorResp.failed("Invalid JSON format:" + exception.getCause().getMessage());
            log.warn("JSON Deserialization Error: ", exception.getCause()); // 记录更具体的错误
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(failed)
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResp.failed("An unexpected error occurred"))
                .build();
    }
}