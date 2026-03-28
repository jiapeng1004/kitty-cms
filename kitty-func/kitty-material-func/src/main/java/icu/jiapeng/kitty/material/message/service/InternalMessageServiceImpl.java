package icu.jiapeng.kitty.material.message.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.material.message.InternalMessageStatuses;
import icu.jiapeng.kitty.material.message.entity.KtInternalMessage;
import icu.jiapeng.kitty.material.message.mapper.KtInternalMessageMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 站内信服务实现。
 */
@Service
public class InternalMessageServiceImpl extends ServiceImpl<KtInternalMessageMapper, KtInternalMessage> implements InternalMessageService {

    @Override
    public void saveMessage(KtInternalMessage message) {
        if (message.getCreateTime() == null) {
            message.setCreateTime(new Date());
        }
        save(message);
    }

    @Override
    public Optional<KtInternalMessage> findById(String id) {
        return Optional.ofNullable(getById(id));
    }

    @Override
    public List<KtInternalMessage> listByReceiverDesc(String receiverUserId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return lambdaQuery()
                .eq(KtInternalMessage::getReceiverUserId, receiverUserId)
                .orderByDesc(KtInternalMessage::getCreateTime)
                .last("LIMIT " + safeLimit)
                .list();
    }

    @Override
    public void markRead(String messageId, String receiverUserId) {
        lambdaUpdate()
                .eq(KtInternalMessage::getId, messageId)
                .eq(KtInternalMessage::getReceiverUserId, receiverUserId)
                .set(KtInternalMessage::getMessageStatus, InternalMessageStatuses.READ)
                .update();
    }
}
