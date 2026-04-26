package icu.jiapeng.kitty.material.searchsync.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import icu.jiapeng.kitty.material.searchsync.MaterialSearchIndexPort;
import icu.jiapeng.kitty.material.searchsync.MaterialSearchSyncMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 消费检索同步消息：全量索引与编目字段 partial update（无文档时回退全量）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialSearchSyncProcessor {

    private final MaterialSearchDocumentBuilder documentBuilder;
    private final MaterialSearchIndexPort materialSearchIndexPort;

    public void processPayloadJson(String payloadJson) {
        if (!StringUtils.hasText(payloadJson)) {
            return;
        }
        try {
            MaterialSearchSyncInboundMessage msg = JSON.parseObject(payloadJson, MaterialSearchSyncInboundMessage.class, Feature.IgnoreNotMatch);
            process(msg);
        } catch (Exception e) {
            log.error("material search sync parse/handle failed: {}", payloadJson, e);
        }
    }

    public void process(MaterialSearchSyncInboundMessage msg) {
        if (msg == null || !StringUtils.hasText(msg.getResourceId()) || msg.getType() == null) {
            log.warn("material search sync skip invalid message: {}", msg);
            return;
        }
        String resourceId = msg.getResourceId();
        try {
            if (msg.getType() == MaterialSearchSyncMessageType.FULL_DOCUMENT) {
                Map<String, Object> doc = documentBuilder.buildFullDocument(resourceId);
                if (doc == null) {
                    log.warn("material search sync resource not found, skip index: {}", resourceId);
                    return;
                }
                materialSearchIndexPort.indexOrReplace(resourceId, doc);
                return;
            }
            if (msg.getType() == MaterialSearchSyncMessageType.METADATA_FIELD_PATCH) {
                // 允许不严格保留旧行为：meta patch 直接回退到全量重建，避免 ES patch 语义差异。
                indexFullFallback(resourceId);
                return;
            }
            if (msg.getType() == MaterialSearchSyncMessageType.DOCUMENT_REMOVED) {
                materialSearchIndexPort.removeDocument(resourceId);
                return;
            }
        } catch (Exception e) {
            log.error("material search sync failed resourceId={} type={}", resourceId, msg.getType(), e);
        }
    }

    private void indexFullFallback(String resourceId) {
        Map<String, Object> doc = documentBuilder.buildFullDocument(resourceId);
        if (doc != null) {
            materialSearchIndexPort.indexOrReplace(resourceId, doc);
        }
    }
}
