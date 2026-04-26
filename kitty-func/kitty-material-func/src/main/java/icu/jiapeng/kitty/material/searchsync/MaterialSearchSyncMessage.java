package icu.jiapeng.kitty.material.searchsync;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 事务提交后投递的检索同步载荷（与具体 MQ/ES 解耦）。
 */
@Value
@Builder(access = AccessLevel.PRIVATE)
public class MaterialSearchSyncMessage {

    String resourceId;
    String catalogId;
    MaterialSearchSyncMessageType type;
    String templateId;
    @Builder.Default
    List<String> touchedFieldCodes = List.of();

    public static MaterialSearchSyncMessage fullDocument(String resourceId, String catalogId) {
        return builder()
                .resourceId(Objects.requireNonNull(resourceId))
                .catalogId(catalogId)
                .type(MaterialSearchSyncMessageType.FULL_DOCUMENT)
                .build();
    }

    public static MaterialSearchSyncMessage metadataFieldPatch(
            String resourceId,
            String catalogId,
            String templateId,
            Collection<String> fieldCodes) {
        return builder()
                .resourceId(Objects.requireNonNull(resourceId))
                .catalogId(catalogId)
                .type(MaterialSearchSyncMessageType.METADATA_FIELD_PATCH)
                .templateId(Objects.requireNonNull(templateId))
                .touchedFieldCodes(fieldCodes == null ? List.of() : List.copyOf(fieldCodes))
                .build();
    }

    public static MaterialSearchSyncMessage documentRemoved(String resourceId, String catalogId) {
        return builder()
                .resourceId(Objects.requireNonNull(resourceId))
                .catalogId(catalogId)
                .type(MaterialSearchSyncMessageType.DOCUMENT_REMOVED)
                .build();
    }
}
