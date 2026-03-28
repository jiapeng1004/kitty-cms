package icu.jiapeng.kitty.material.message.service;

import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.message.InternalMessageStatuses;
import icu.jiapeng.kitty.material.message.dto.MaterialInternalMessageSendDTO;
import icu.jiapeng.kitty.material.message.entity.KtInternalMessage;
import icu.jiapeng.kitty.material.message.vo.MaterialInternalMessageVO;
import icu.jiapeng.kitty.material.user.UserContextGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialInternalMessageService {

    private static final int INBOX_LIMIT = 50;

    private final InternalMessageService internalMessageService;
    private final UserContextGateway userContextGateway;
    private final MaterialInternalMessageBroadcastPublisher broadcastPublisher;

    @Transactional(rollbackFor = Exception.class)
    public MaterialInternalMessageVO send(MaterialInternalMessageSendDTO req) {
        if (req == null || !StringUtils.hasText(req.receiverUserId()) || !StringUtils.hasText(req.content())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String sender = userContextGateway.currentUserId();
        if (sender.equals(req.receiverUserId().trim())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtInternalMessage m = new KtInternalMessage();
        m.setId(UUID.randomUUID().toString());
        m.setSenderUserId(sender);
        m.setReceiverUserId(req.receiverUserId().trim());
        m.setContent(req.content().trim());
        m.setMessageStatus(InternalMessageStatuses.UNREAD);
        m.setCreateTime(new Date());
        // 注意：这里使用 Date 类型，因为 CommonEntity 中的时间字段是 Date 类型
        internalMessageService.saveMessage(m);
        broadcastPublisher.publishNewMessage(m.getReceiverUserId(), m.getId());
        return toVo(m);
    }

    public List<MaterialInternalMessageVO> listMine() {
        String uid = userContextGateway.currentUserId();
        return internalMessageService.listByReceiverDesc(uid, INBOX_LIMIT).stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void markRead(String messageId) {
        if (!StringUtils.hasText(messageId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String uid = userContextGateway.currentUserId();
        internalMessageService.markRead(messageId.trim(), uid);
    }

    private MaterialInternalMessageVO toVo(KtInternalMessage m) {
        return new MaterialInternalMessageVO(
                m.getId(),
                m.getSenderUserId(),
                m.getReceiverUserId(),
                m.getContent(),
                m.getMessageStatus(),
                m.getCreateTime() != null ? m.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
    }
}
