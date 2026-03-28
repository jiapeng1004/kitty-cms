package icu.jiapeng.kitty.material.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "站内信发送请求")
public record MaterialInternalMessageSendDTO(
        @NotBlank @Schema(description = "接收者用户 ID") String receiverUserId,
        @NotBlank @Schema(description = "消息正文") String content
) {
}
