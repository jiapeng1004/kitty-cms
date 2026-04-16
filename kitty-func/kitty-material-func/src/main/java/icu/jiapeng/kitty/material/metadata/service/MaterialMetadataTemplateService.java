package icu.jiapeng.kitty.material.metadata.service;

import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.catalog.service.CatalogService;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataFieldBindItemDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataTemplateBindFieldsDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataTemplateQueryDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataTemplateUpsertDTO;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataField;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataTemplate;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataTemplateFieldBinding;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataFormFieldVO;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataTemplateVO;
import icu.jiapeng.kitty.material.catalog.constants.CatalogPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 编目模板：栏目/资源类型维度的模板 CRUD，以及模板与字段的绑定与排序（表单渲染顺序）。
 */
@Service
@RequiredArgsConstructor
public class MaterialMetadataTemplateService {

    private final MetadataTemplateService metadataTemplateService;
    private final MetadataTemplateFieldBindingService metadataTemplateFieldBindingService;
    private final MetadataFieldService metadataFieldService;
    private final CatalogService catalogService;
    private final MetadataFormFieldAssembler metadataFormFieldAssembler;

    public List<MaterialMetadataFormFieldVO> listBindingFields(String templateId) {
        if (isBlank(templateId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtMetadataTemplate t = metadataTemplateService.findById(templateId)
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(t.getCatalogId(), CatalogPermission.METADATA_TEMPLATE_MANAGE);
        return metadataFormFieldAssembler.orderedFormFields(templateId);
    }

    public MaterialMetadataTemplateVO create(MaterialMetadataTemplateUpsertDTO req) {
        validateUpsert(req, false);
        catalogService.requireOnCatalog(req.getCatalogId(), CatalogPermission.METADATA_TEMPLATE_MANAGE);
        KtMetadataTemplate t = new KtMetadataTemplate();
        t.setId(UUID.randomUUID().toString());
        applyUpsert(t, req);
        metadataTemplateService.saveTemplate(t);
        return toVo(t);
    }

    public MaterialMetadataTemplateVO update(MaterialMetadataTemplateUpsertDTO req) {
        validateUpsert(req, true);
        KtMetadataTemplate existing = metadataTemplateService.findById(req.getId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(existing.getCatalogId(), CatalogPermission.METADATA_TEMPLATE_MANAGE);
        if (!Objects.equals(existing.getCatalogId(), req.getCatalogId())) {
            catalogService.requireOnCatalog(req.getCatalogId(), CatalogPermission.METADATA_TEMPLATE_MANAGE);
        }
        applyUpsert(existing, req);
        metadataTemplateService.saveTemplate(existing);
        return toVo(existing);
    }

    public void delete(String id) {
        if (isBlank(id)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtMetadataTemplate existing = metadataTemplateService.findById(id)
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(existing.getCatalogId(), CatalogPermission.METADATA_TEMPLATE_MANAGE);
        metadataTemplateFieldBindingService.replaceBindings(id, List.of());
        KtMetadataTemplate template = metadataTemplateService.findById(id)
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        metadataTemplateService.delete(template);
    }

    public List<MaterialMetadataTemplateVO> list(MaterialMetadataTemplateQueryDTO query) {
        return metadataTemplateService.findAll()
                .stream()
                .filter(t -> query == null || isBlank(query.getCatalogId()) || query.getCatalogId().equals(t.getCatalogId()))
                .filter(t -> query == null || query.getResourceType() == null || Objects.equals(query.getResourceType(), t.getResourceType()))
                .filter(t -> query == null || !Boolean.TRUE.equals(query.getEnabledOnly()) || Objects.equals(1, t.getEnabled()))
                .map(this::toVo)
                .toList();
    }

    public void bindFields(MaterialMetadataTemplateBindFieldsDTO req) {
        if (req == null || isBlank(req.getTemplateId()) || req.getBindings() == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtMetadataTemplate template = metadataTemplateService.findById(req.getTemplateId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(template.getCatalogId(), CatalogPermission.METADATA_TEMPLATE_MANAGE);
        List<KtMetadataTemplateFieldBinding> rows = new ArrayList<>();
        for (MaterialMetadataFieldBindItemDTO item : req.getBindings()) {
            if (item == null || isBlank(item.getFieldId())) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            KtMetadataField field = metadataFieldService.findById(item.getFieldId())
                    .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
            KtMetadataTemplateFieldBinding b = new KtMetadataTemplateFieldBinding();
            b.setId(UUID.randomUUID().toString());
            b.setTemplateId(template.getId());
            b.setFieldId(field.getId());
            b.setSortNum(item.getSortNum() == null ? 0 : item.getSortNum());
            rows.add(b);
        }
        metadataTemplateFieldBindingService.replaceBindings(template.getId(), rows);
    }

    private void validateUpsert(MaterialMetadataTemplateUpsertDTO req, boolean update) {
        if (req == null || isBlank(req.getName()) || isBlank(req.getCatalogId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (update && isBlank(req.getId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
    }

    private void applyUpsert(KtMetadataTemplate t, MaterialMetadataTemplateUpsertDTO req) {
        t.setName(req.getName());
        t.setCatalogId(req.getCatalogId());
        t.setResourceType(req.getResourceType());
        t.setEnabled(req.getEnabled() == null ? 1 : req.getEnabled());
    }

    private MaterialMetadataTemplateVO toVo(KtMetadataTemplate t) {
        MaterialMetadataTemplateVO vo = new MaterialMetadataTemplateVO();
        vo.setId(t.getId());
        vo.setName(t.getName());
        vo.setCatalogId(t.getCatalogId());
        vo.setResourceType(t.getResourceType());
        vo.setEnabled(t.getEnabled());
        return vo;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
