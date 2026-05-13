package icu.jiapeng.kitty.material.message.api;

import icu.jiapeng.kitty.material.message.dto.MaterialInternalMessageSendDTO;
import icu.jiapeng.kitty.material.message.vo.MaterialInternalMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 站内信与 SSE（OpenFeign + MVC 契约）。
 */
@Tag(name = "Material-站内信")
@FeignClient(name = "kitty-mam", contextId = "materialInternalMessage")
public interface MaterialInternalMessageApi {

    @Operation(summary = "发送站内信")
    @PostMapping("/api/message/send")
    MaterialInternalMessageVO send(@Valid @RequestBody MaterialInternalMessageSendDTO req);

    @Operation(summary = "收件箱列表")
    @GetMapping("/api/message/list")
    List<MaterialInternalMessageVO> list();

    @Operation(summary = "标记已读")
    @PostMapping("/api/message/read")
    void markRead(@RequestParam String messageId);

    @Operation(summary = "SSE 实时推送（event:new-message，data 为 messageId）")
    @GetMapping(value = "/message/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter sse();
}
