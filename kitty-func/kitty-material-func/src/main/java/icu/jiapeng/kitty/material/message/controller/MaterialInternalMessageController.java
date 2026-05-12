package icu.jiapeng.kitty.material.message.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.message.service.MaterialInternalMessageService;
import icu.jiapeng.kitty.material.message.service.MaterialInternalMessageSseRegistry;
import icu.jiapeng.kitty.material.user.UserContextGateway;
import icu.jiapeng.kitty.material.message.api.MaterialInternalMessageApi;
import icu.jiapeng.kitty.material.message.dto.MaterialInternalMessageSendDTO;
import icu.jiapeng.kitty.material.message.vo.MaterialInternalMessageVO;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 站内信与 SSE。
 */
@RestController
@RequiredArgsConstructor
@Validated
public class MaterialInternalMessageController implements MaterialInternalMessageApi {

    private static final long SSE_TIMEOUT_MS = 30L * 60 * 1000;

    private final MaterialInternalMessageService internalMessageService;
    private final MaterialInternalMessageSseRegistry sseRegistry;
    private final UserContextGateway userContextGateway;

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_MESSAGE_SEND)
    public MaterialInternalMessageVO send(MaterialInternalMessageSendDTO req) {
        return internalMessageService.send(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_MESSAGE_READ)
    public List<MaterialInternalMessageVO> list() {
        return internalMessageService.listMine();
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_MESSAGE_READ)
    public void markRead(String messageId) {
        internalMessageService.markRead(messageId);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_MESSAGE_READ)
    public SseEmitter sse() {
        String uid = userContextGateway.currentUserId();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        sseRegistry.register(uid, emitter);
        Runnable detach = () -> sseRegistry.remove(uid, emitter);
        emitter.onCompletion(detach);
        emitter.onTimeout(detach);
        emitter.onError(e -> detach.run());
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (Exception ignored) {
            // 连接立即失败时由 onCompletion 清理
        }
        return emitter;
    }
}
