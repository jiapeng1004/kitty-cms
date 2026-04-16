package icu.jiapeng.kitty.material.metadata.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.catalog.service.CatalogService;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataHistoryQueryDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataLastQueryDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataSaveDTO;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataField;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataInstance;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataTemplate;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataTemplateFieldBinding;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataFormFieldVO;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataInstanceEntryVO;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataSnapshotVO;
import icu.jiapeng.kitty.material.catalog.constants.CatalogPermission;
import icu.jiapeng.kitty.material.resource.entity.KtResource;
import icu.jiapeng.kitty.material.resource.service.MaterialResourceService;
import icu.jiapeng.kitty.material.searchsync.service.MaterialSearchSyncTrigger;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MaterialMetadataInstanceService {

    @Resource
    private MetadataInstanceService metadataInstanceService;
    @Resource
    private MetadataTemplateService metadataTemplateService;
    @Resource
    private MetadataTemplateFieldBindingService metadataTemplateFieldBindingService;
    @Resource
    private MetadataFieldService metadataFieldService;
    @Resource
    @Lazy
    private MaterialResourceService resourceService;
    @Resource
    private CatalogService catalogService;
    @Resource
    private MetadataFormFieldAssembler metadataFormFieldAssembler;
    @Resource
    private MaterialSearchSyncTrigger materialSearchSyncTrigger;

    /**
     * 获取资源的表单字段
     *
     * @param resourceId 资源ID
     * @param templateId 模板ID
     * @return 表单字段列表
     */
    public List<MaterialMetadataFormFieldVO> formFieldsForResource(String resourceId, String templateId) {
        if (!StringUtils.hasText(resourceId) || !StringUtils.hasText(templateId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = resourceService.findById(resourceId)
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), CatalogPermission.RESOURCE_LIST_VIEW);
        KtMetadataTemplate template = metadataTemplateService.findById(templateId)
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        assertTemplateApplies(template, resource);
        return metadataFormFieldAssembler.orderedFormFields(templateId);
    }

    /**
     * 保存元数据实例
     *
     * @param req 保存请求
     * @return 元数据快照VO
     */
    @Transactional(rollbackFor = Exception.class)
    public MaterialMetadataSnapshotVO save(MaterialMetadataSaveDTO req) {
        if (req == null || !StringUtils.hasText(req.getResourceId()) || !StringUtils.hasText(req.getTemplateId()) || req.getFieldValues() == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = resourceService.findById(req.getResourceId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), CatalogPermission.RESOURCE_UPDATE);
        KtMetadataTemplate template = metadataTemplateService.findById(req.getTemplateId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        assertTemplateApplies(template, resource);
        List<KtMetadataTemplateFieldBinding> bindings = metadataTemplateFieldBindingService.listByTemplateIdOrderBySort(template.getId());
        if (bindings.isEmpty()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        Map<String, String> values = req.getFieldValues();
        List<KtMetadataInstance> newRows = new ArrayList<>();
        List<String> touchedFieldCodes = new ArrayList<>();
        for (KtMetadataTemplateFieldBinding bind : bindings) {
            KtMetadataField field = metadataFieldService.findById(bind.getFieldId())
                    .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
            touchedFieldCodes.add(field.getFieldCode());
            String raw = values.get(field.getFieldCode());
            validateFieldValue(field, raw);
            String stored = raw == null ? "" : raw;
            KtMetadataInstance row = new KtMetadataInstance();
            row.setId(UUID.randomUUID().toString());
            row.setResourceId(resource.getId());
            row.setTemplateId(template.getId());
            row.setFieldId(field.getId());
            row.setFieldValue(stored);
            newRows.add(row);
        }
        metadataInstanceService.clearLastFlag(resource.getId(), template.getId());
        int nextVer = metadataInstanceService.findMaxVersion(resource.getId(), template.getId()) + 1;
        for (KtMetadataInstance row : newRows) {
            row.setVersion(nextVer);
            row.setLastVersion(1);
        }
        metadataInstanceService.saveAll(newRows);
        materialSearchSyncTrigger.publishMetadataFieldPatch(resource.getId(), resource.getCatalogId(), template.getId(), touchedFieldCodes);
        return toSnapshot(template, newRows);
    }

    /**
     * 获取最新的元数据快照
     *
     * @param query 查询参数
     * @return 元数据快照VO
     */
    public MaterialMetadataSnapshotVO getLast(MaterialMetadataLastQueryDTO query) {
        if (query == null || !StringUtils.hasText(query.getResourceId()) || !StringUtils.hasText(query.getTemplateId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = resourceService.findById(query.getResourceId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), CatalogPermission.RESOURCE_LIST_VIEW);
        KtMetadataTemplate template = metadataTemplateService.findById(query.getTemplateId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        assertAlignedCatalog(template, resource);
        List<KtMetadataInstance> rows = metadataInstanceService.findLast(query.getResourceId(), query.getTemplateId());
        if (rows.isEmpty()) {
            MaterialMetadataSnapshotVO empty = new MaterialMetadataSnapshotVO();
            empty.setTemplateId(template.getId());
            empty.setTemplateName(template.getName());
            empty.setVersion(null);
            empty.setEntries(List.of());
            return empty;
        }
        return toSnapshot(template, rows);
    }

    /**
     * 获取元数据历史记录
     *
     * @param query 查询参数
     * @return 元数据实例条目VO列表
     */
    public List<MaterialMetadataInstanceEntryVO> history(MaterialMetadataHistoryQueryDTO query) {
        if (query == null || !StringUtils.hasText(query.getResourceId()) || !StringUtils.hasText(query.getTemplateId()) || query.getMaxVersion() == null || query.getMaxVersion() < 1) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = resourceService.findById(query.getResourceId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), CatalogPermission.RESOURCE_LIST_VIEW);
        KtMetadataTemplate template = metadataTemplateService.findById(query.getTemplateId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        assertAlignedCatalog(template, resource);
        List<KtMetadataInstance> rows = metadataInstanceService.findByResourceTemplateVersionUpTo(
                query.getResourceId(), query.getTemplateId(), query.getMaxVersion());
        List<MaterialMetadataInstanceEntryVO> out = new ArrayList<>();
        for (KtMetadataInstance row : rows) {
            out.add(toEntry(template.getId(), row));
        }
        return out;
    }

    public List<MaterialMetadataSnapshotVO> snapshotsForSearchIndex(String resourceId) {
        if (!StringUtils.hasText(resourceId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = resourceService.findById(resourceId)
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        List<KtMetadataTemplate> templates = metadataTemplateService.listEnabledByCatalogAndType(resource.getCatalogId(), resource.getType());
        List<MaterialMetadataSnapshotVO> out = new ArrayList<>();
        for (KtMetadataTemplate t : templates) {
            List<KtMetadataInstance> rows = metadataInstanceService.findLast(resourceId, t.getId());
            if (!rows.isEmpty()) {
                out.add(toSnapshot(t, rows));
            }
        }
        return out;
    }

    public List<MaterialMetadataSnapshotVO> snapshotsForResourceDetail(String resourceId) {
        if (!StringUtils.hasText(resourceId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = resourceService.findById(resourceId)
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), CatalogPermission.RESOURCE_LIST_VIEW);
        List<KtMetadataTemplate> templates = metadataTemplateService.listEnabledByCatalogAndType(resource.getCatalogId(), resource.getType());
        List<MaterialMetadataSnapshotVO> out = new ArrayList<>();
        for (KtMetadataTemplate t : templates) {
            List<KtMetadataInstance> rows = metadataInstanceService.findLast(resourceId, t.getId());
            if (!rows.isEmpty()) {
                out.add(toSnapshot(t, rows));
            }
        }
        return out;
    }

    private void assertTemplateApplies(KtMetadataTemplate template, KtResource resource) {
        assertAlignedCatalog(template, resource);
        if (!Objects.equals(1, template.getEnabled())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (template.getResourceType() != null && !template.getResourceType().equals(resource.getType())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
    }

    private void assertAlignedCatalog(KtMetadataTemplate template, KtResource resource) {
        if (!Objects.equals(template.getCatalogId(), resource.getCatalogId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
    }

    private void validateFieldValue(KtMetadataField field, String value) {
        if (field.getRequired() != null && field.getRequired() == 1) {
            if (value == null || value.isBlank()) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
        }
        if (value == null || value.isBlank()) {
            return;
        }
        if (!"SELECT".equalsIgnoreCase(field.getInputType()) || field.getOptionsJson() == null || field.getOptionsJson().isBlank()) {
            return;
        }
        try {
            JSONArray arr = JSON.parseArray(field.getOptionsJson());
            if (arr == null || arr.isEmpty()) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            boolean ok = false;
            for (int i = 0; i < arr.size(); i++) {
                String opt = arr.getString(i);
                if (value.equals(opt)) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
    }

    private MaterialMetadataSnapshotVO toSnapshot(KtMetadataTemplate template, List<KtMetadataInstance> rows) {
        MaterialMetadataSnapshotVO s = new MaterialMetadataSnapshotVO();
        s.setTemplateId(template.getId());
        s.setTemplateName(template.getName());
        s.setVersion(rows.isEmpty() ? null : rows.get(0).getVersion());
        List<MaterialMetadataInstanceEntryVO> entries = new ArrayList<>();
        for (KtMetadataInstance row : rows) {
            entries.add(toEntry(template.getId(), row));
        }
        s.setEntries(entries);
        return s;
    }

    private MaterialMetadataInstanceEntryVO toEntry(String templateId, KtMetadataInstance row) {
        KtMetadataField field = metadataFieldService.findById(row.getFieldId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        MaterialMetadataInstanceEntryVO e = new MaterialMetadataInstanceEntryVO();
        e.setFieldId(field.getId());
        e.setFieldCode(field.getFieldCode());
        e.setFieldName(field.getFieldName());
        e.setFieldValue(row.getFieldValue());
        e.setVersion(row.getVersion());
        e.setLastVersion(row.getLastVersion());
        e.setTemplateId(templateId);
        return e;
    }
}
