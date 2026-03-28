package icu.jiapeng.kitty.material.searchsync.service;

import icu.jiapeng.kitty.material.searchsync.MaterialSearchSyncMessageType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 与 {@link icu.jiapeng.kitty.material.searchsync.MaterialSearchSyncMessage} JSON 对齐，供消费者反序列化。
 */
@Data
public class MaterialSearchSyncInboundMessage {

    private String resourceId;
    private String catalogId;
    private MaterialSearchSyncMessageType type;
    private String templateId;
    private List<String> touchedFieldCodes = new ArrayList<>();
}
