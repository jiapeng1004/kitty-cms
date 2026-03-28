package icu.jiapeng.kitty.material.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.message.entity.KtInternalMessage;

import java.util.List;
import java.util.Optional;

/**
 * 站内信服务接口。
 */
public interface InternalMessageService extends IService<KtInternalMessage> {

    void saveMessage(KtInternalMessage message);

    Optional<KtInternalMessage> findById(String id);

    List<KtInternalMessage> listByReceiverDesc(String receiverUserId, int limit);

    void markRead(String messageId, String receiverUserId);
}
