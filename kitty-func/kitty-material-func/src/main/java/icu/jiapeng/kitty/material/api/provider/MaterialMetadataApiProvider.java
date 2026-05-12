package icu.jiapeng.kitty.material.api.provider;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.metadata.api.MaterialMetadataApi;
import icu.jiapeng.kitty.material.metadata.dto.*;
import icu.jiapeng.kitty.material.metadata.service.MaterialMetadataFieldService;
import icu.jiapeng.kitty.material.metadata.service.MaterialMetadataInstanceService;
import icu.jiapeng.kitty.material.metadata.service.MaterialMetadataTemplateService;
import icu.jiapeng.kitty.material.metadata.vo.*;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 与 {@link icu.jiapeng.kitty.material.metadata.controller.MaterialMetadataController} 同逻辑；
 * 与 Controller 通过 profile {@code kitty-mam-embedded-api} 互斥。
 */
@Primary
@Service
@RequiredArgsConstructor
@Validated
public class MaterialMetadataApiProvider implements MaterialMetadataApi {

    private final MaterialMetadataTemplateService templateService;
    private final MaterialMetadataFieldService fieldService;
    private final MaterialMetadataInstanceService instanceService;

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_METADATA_TEMPLATE_MANAGE)
    public MaterialMetadataTemplateVO createTemplate(MaterialMetadataTemplateUpsertDTO req) {
        return templateService.create(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_METADATA_TEMPLATE_MANAGE)
    public MaterialMetadataTemplateVO updateTemplate(MaterialMetadataTemplateUpsertDTO req) {
        return templateService.update(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_METADATA_TEMPLATE_MANAGE)
    public void deleteTemplate(String id) {
        templateService.delete(id);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_METADATA_TEMPLATE_MANAGE)
    public List<MaterialMetadataTemplateVO> listTemplates(MaterialMetadataTemplateQueryDTO query) {
        return templateService.list(query);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_METADATA_TEMPLATE_MANAGE)
    public void bindTemplateFields(MaterialMetadataTemplateBindFieldsDTO req) {
        templateService.bindFields(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_METADATA_TEMPLATE_MANAGE)
    public List<MaterialMetadataFormFieldVO> listTemplateBindings(String templateId) {
        return templateService.listBindingFields(templateId);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_METADATA_FIELD_MANAGE)
    public MaterialMetadataFieldVO createField(MaterialMetadataFieldUpsertDTO req) {
        return fieldService.create(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_METADATA_FIELD_MANAGE)
    public MaterialMetadataFieldVO updateField(MaterialMetadataFieldUpsertDTO req) {
        return fieldService.update(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_METADATA_FIELD_MANAGE)
    public void deleteField(String id) {
        fieldService.delete(id);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_METADATA_FIELD_MANAGE)
    public List<MaterialMetadataFieldVO> listFields() {
        return fieldService.listAll();
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public List<MaterialMetadataFormFieldVO> formFieldsForResource(String resourceId, String templateId) {
        return instanceService.formFieldsForResource(resourceId, templateId);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE)
    public MaterialMetadataSnapshotVO saveInstance(MaterialMetadataSaveDTO req) {
        return instanceService.save(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public MaterialMetadataSnapshotVO getLast(MaterialMetadataLastQueryDTO query) {
        return instanceService.getLast(query);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public List<MaterialMetadataInstanceEntryVO> history(MaterialMetadataHistoryQueryDTO query) {
        return instanceService.history(query);
    }
}
