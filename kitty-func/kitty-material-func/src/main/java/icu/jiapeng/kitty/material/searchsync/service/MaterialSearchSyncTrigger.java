package icu.jiapeng.kitty.material.searchsync.service;

import com.alibaba.fastjson.JSON;
import icu.jiapeng.kitty.material.resource.entity.KtResource;
import icu.jiapeng.kitty.material.searchsync.MaterialSearchSyncChannels;
import icu.jiapeng.kitty.material.searchsync.MaterialSearchSyncMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;

/**
 * 检索同步异步触发：事务提交后向 Kafka topic {@link MaterialSearchSyncChannels#SEARCH_SYNC} 投递 JSON。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaterialSearchSyncTrigger {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${material.search.sync.kafka.topic:material.search.sync}")
    private String searchSyncTopic;

    public void publishFullDocument(KtResource resource) {
        if (resource == null || resource.getId() == null || resource.getId().isBlank()) {
            return;
        }
        MaterialSearchSyncMessage msg = MaterialSearchSyncMessage.fullDocument(resource.getId(), resource.getCatalogId());
        scheduleAfterCommit(() -> sendSafe(msg));
    }

    public void publishMetadataFieldPatch(String resourceId, String catalogId, String templateId, Collection<String> fieldCodes) {
        if (resourceId == null || resourceId.isBlank()) {
            return;
        }
        MaterialSearchSyncMessage msg = MaterialSearchSyncMessage.metadataFieldPatch(resourceId, catalogId, templateId, fieldCodes);
        scheduleAfterCommit(() -> sendSafe(msg));
    }

    private void sendSafe(MaterialSearchSyncMessage msg) {
        try {
            String json = JSON.toJSONString(msg);
            kafkaTemplate.send(searchSyncTopic, msg.getResourceId(), json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("material search sync kafka send failed, msg={}", msg, ex);
                        }
                    });
        } catch (Exception ex) {
            log.error("material search sync kafka send failed, msg={}", msg, ex);
        }
    }

    private void scheduleAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
