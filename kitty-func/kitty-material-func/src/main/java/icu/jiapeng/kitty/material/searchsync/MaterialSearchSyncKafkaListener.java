package icu.jiapeng.kitty.material.searchsync;

import icu.jiapeng.kitty.material.searchsync.service.MaterialSearchSyncProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 检索同步 Kafka 消费：与 {@link icu.jiapeng.kitty.material.searchsync.service.MaterialSearchSyncTrigger} 生产端共用
 * topic {@link icu.jiapeng.kitty.material.searchsync.MaterialSearchSyncChannels#SEARCH_SYNC}（可通过 {@code material.search.sync.kafka.topic} 覆盖）。
 */
@Component
@RequiredArgsConstructor
public class MaterialSearchSyncKafkaListener {

    private final MaterialSearchSyncProcessor materialSearchSyncProcessor;

    @KafkaListener(
            topics = "${material.search.sync.kafka.topic:material.search.sync}",
            groupId = "${spring.kafka.consumer.group-id:kitty-mam-search-sync}"
    )
    public void onMessage(String payload) {
        materialSearchSyncProcessor.processPayloadJson(payload);
    }
}
