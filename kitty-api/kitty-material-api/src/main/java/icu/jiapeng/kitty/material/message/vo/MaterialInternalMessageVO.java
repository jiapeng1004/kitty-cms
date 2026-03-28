package icu.jiapeng.kitty.material.message.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "站内信 VO")
public record MaterialInternalMessageVO(
        String id,
        String senderUserId,
        String receiverUserId,
        String content,
        @Schema(description = "unread / read") String messageStatus,
        LocalDateTime createdAt
) {
}
