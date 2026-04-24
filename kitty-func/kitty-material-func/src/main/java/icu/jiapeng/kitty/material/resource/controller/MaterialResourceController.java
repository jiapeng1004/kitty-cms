package icu.jiapeng.kitty.material.resource.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.resource.service.MaterialResourceService;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import icu.jiapeng.kitty.material.resource.api.MaterialResourceApi;
import icu.jiapeng.kitty.material.resource.dto.MaterialMetaFileBindDTO;
import icu.jiapeng.kitty.material.resource.dto.MaterialFolderCreateDTO;
import icu.jiapeng.kitty.material.resource.dto.MaterialFolderUploadPlanDTO;
import icu.jiapeng.kitty.material.resource.dto.MaterialDownloadReportBatchDTO;
import icu.jiapeng.kitty.material.resource.dto.MaterialDownloadReportItemDTO;
import icu.jiapeng.kitty.material.resource.dto.MaterialResourceFingerprintDTO;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.material.resource.dto.MaterialResourceListPageQueryDTO;
import icu.jiapeng.kitty.material.resource.dto.MaterialResourceListQueryDTO;
import icu.jiapeng.kitty.material.resource.dto.MaterialResourceUpsertDTO;
import icu.jiapeng.kitty.material.resource.vo.MaterialMetaFileVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceDetailVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialDownloadUrlVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceFingerprintPrecheckVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * 资源控制器。
 */
@RestController
@RequiredArgsConstructor
@Validated
public class MaterialResourceController implements MaterialResourceApi {

    private final MaterialResourceService materialResourceService;

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public List<MaterialResourceVO> list(MaterialResourceListQueryDTO query) {
        return materialResourceService.list(query);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public PageRespVo<MaterialResourceVO> page(MaterialResourceListPageQueryDTO query) {
        return materialResourceService.page(query);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public MaterialResourceDetailVO detail(String resourceId) {
        return materialResourceService.detail(resourceId);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public ResponseEntity<Void> preview(String resourceId) {
        String target = materialResourceService.resolvePreviewRedirectUrl(resourceId);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public ResponseEntity<Void> keyframe(String resourceId) {
        return materialResourceService.resolveKeyframeRedirectUrl(resourceId)
                .<ResponseEntity.HeadersBuilder<?>>map(url -> ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)))
                .orElse(ResponseEntity.notFound()).build();
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_CREATE)
    public MaterialResourceVO create(MaterialResourceUpsertDTO req) {
        return materialResourceService.create(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE)
    public MaterialResourceVO update(MaterialResourceUpsertDTO req) {
        return materialResourceService.update(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_CREATE)
    public MaterialResourceVO createFolder(MaterialFolderCreateDTO req) {
        return materialResourceService.createFolder(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE)
    public void rebuildPath(MaterialResourceListQueryDTO query) {
        materialResourceService.rebuildPath(query);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_CREATE)
    public List<MaterialResourceVO> planFolderUpload(MaterialFolderUploadPlanDTO req) {
        return materialResourceService.planFolderUpload(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE)
    public MaterialResourceVO saveFingerprint(MaterialResourceFingerprintDTO req) {
        return materialResourceService.saveFingerprint(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public MaterialResourceFingerprintPrecheckVO precheckFingerprint(MaterialResourceFingerprintDTO req) {
        return materialResourceService.precheckFingerprint(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public ResponseEntity<MaterialMetaFileVO> getMetaFile(String resourceId) {
        return materialResourceService.findMetaFileByResource(resourceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE)
    public MaterialMetaFileVO bindMetaFile(MaterialMetaFileBindDTO req) {
        return materialResourceService.bindMetaFile(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public MaterialDownloadUrlVO downloadUrl(String resourceId, String destinationType) {
        return materialResourceService.resolveDownloadUrl(resourceId, destinationType);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public void reportDownload(MaterialDownloadReportItemDTO body) {
        materialResourceService.reportDownload(body);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public void reportDownloadBatch(MaterialDownloadReportBatchDTO body) {
        materialResourceService.reportDownloadBatch(body);
    }
}
