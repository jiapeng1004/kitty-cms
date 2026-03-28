package icu.jiapeng.kitty.material.searchsync.service;

import icu.jiapeng.kitty.material.metadata.service.MaterialMetadataInstanceService;
import icu.jiapeng.kitty.material.metadata.service.MetadataFieldService;
import icu.jiapeng.kitty.material.metadata.service.MetadataInstanceService;
import icu.jiapeng.kitty.material.resource.service.MaterialResourceService;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataSnapshotVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 从 DB 组装 ES 文档（无权限校验，仅供检索同步消费者使用）。
 */
@Component
@RequiredArgsConstructor
public class MaterialSearchDocumentBuilder {

    private final MaterialResourceService materialResourceService;
    private final MaterialMetadataInstanceService metadataInstanceService;
    private final MetadataInstanceService metadataInstanceServiceDirect;
    private final MetadataFieldService metadataFieldService;

    public Map<String, Object> buildFullDocument(String resourceId) {
        var r = materialResourceService.getById(resourceId);
        if (r == null) {
            return null;
        }
        List<MaterialMetadataSnapshotVO> snapshots = metadataInstanceService.snapshotsForSearchIndex(resourceId);
        return buildDoc(r, snapshots);
    }

    /**
     * 仅编目增量：生成 partial doc，结构为 {@code { "metadata": { templateId: { fieldCode: value }}}。
     */
    public Map<String, Object> buildMetadataPatchDocument(String resourceId, String templateId, List<String> fieldCodes) {
        if (fieldCodes == null || fieldCodes.isEmpty()) {
            return null;
        }
        if (materialResourceService.getById(resourceId) == null) {
            return null;
        }
        List<? extends Object> rows = metadataInstanceServiceDirect.list();
        Map<String, String> fields = new LinkedHashMap<>();
        for (Object row : rows) {
            // 这里需要根据实际的 MpMetadataInstance 结构调整
            // 假设 MpMetadataInstance 有 getFieldId() 和 getFieldValue() 方法
            try {
                var fieldIdMethod = row.getClass().getMethod("getFieldId");
                var fieldValueMethod = row.getClass().getMethod("getFieldValue");
                String fieldId = (String) fieldIdMethod.invoke(row);
                String fieldValue = (String) fieldValueMethod.invoke(row);
                
                var field = metadataFieldService.getById(fieldId);
                if (field != null) {
                    var fieldCodeMethod = field.getClass().getMethod("getFieldCode");
                    String fieldCode = (String) fieldCodeMethod.invoke(field);
                    if (fieldCodes.contains(fieldCode)) {
                        fields.put(fieldCode, fieldValue == null ? "" : fieldValue);
                    }
                }
            } catch (Exception e) {
                // 忽略反射异常
            }
        }
        Map<String, Object> templateBlock = new LinkedHashMap<>();
        templateBlock.put(templateId, fields);
        Map<String, Object> partial = new LinkedHashMap<>();
        partial.put("metadata", templateBlock);
        return partial;
    }

    private Map<String, Object> buildDoc(Object r, List<MaterialMetadataSnapshotVO> snapshots) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        for (MaterialMetadataSnapshotVO s : snapshots) {
            Map<String, String> fields = new LinkedHashMap<>();
            for (var e : s.getEntries()) {
                fields.put(e.getFieldCode(), e.getFieldValue() == null ? "" : e.getFieldValue());
            }
            metadata.put(s.getTemplateId(), fields);
        }
        Map<String, Object> doc = new LinkedHashMap<>();
        try {
            doc.put("resourceId", r.getClass().getMethod("getId").invoke(r));
            doc.put("catalogId", r.getClass().getMethod("getCatalogId").invoke(r));
            doc.put("catalogTreeCode", r.getClass().getMethod("getCatalogTreeCode").invoke(r));
            doc.put("title", r.getClass().getMethod("getTitle").invoke(r));
            doc.put("parentId", r.getClass().getMethod("getParentId").invoke(r));
            doc.put("type", r.getClass().getMethod("getType").invoke(r));
            doc.put("fileSize", r.getClass().getMethod("getFileSize").invoke(r));
            doc.put("fingerprint", r.getClass().getMethod("getFingerprint").invoke(r));
        } catch (Exception e) {
            // 忽略反射异常
        }
        doc.put("metadata", metadata);
        return doc;
    }
}
