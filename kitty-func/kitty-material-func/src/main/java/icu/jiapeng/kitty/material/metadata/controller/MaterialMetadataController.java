package icu.jiapeng.kitty.material.metadata.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.metadata.service.MaterialMetadataFieldService;
import icu.jiapeng.kitty.material.metadata.service.MaterialMetadataInstanceService;
import icu.jiapeng.kitty.material.metadata.service.MaterialMetadataTemplateService;
import icu.jiapeng.kitty.material.metadata.api.MaterialMetadataApi;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataFieldUpsertDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataHistoryQueryDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataLastQueryDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataSaveDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataTemplateBindFieldsDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataTemplateQueryDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataTemplateUpsertDTO;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataFieldVO;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataFormFieldVO;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataInstanceEntryVO;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataSnapshotVO;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataTemplateVO;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class MaterialMetadataController implements MaterialMetadataApi {

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
