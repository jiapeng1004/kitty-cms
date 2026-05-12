/*
 * Copyright [2025] [贾鹏]
 */
package icu.jiapeng.kitty.user.internal.dto;

/**
 * token 自省结果体（无外层业务包装）。
 */
public record TokenIntrospectionPayload(boolean valid) {
}
