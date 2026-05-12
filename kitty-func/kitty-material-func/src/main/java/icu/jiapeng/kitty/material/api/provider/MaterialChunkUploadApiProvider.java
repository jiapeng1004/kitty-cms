package icu.jiapeng.kitty.material.api.provider;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import icu.jiapeng.kitty.material.upload.api.MaterialChunkUploadApi;
import icu.jiapeng.kitty.material.upload.dto.MaterialChunkUploadPartReportDTO;
import icu.jiapeng.kitty.material.upload.dto.MaterialChunkUploadSessionCreateDTO;
import icu.jiapeng.kitty.material.upload.service.MaterialChunkUploadService;
import icu.jiapeng.kitty.material.upload.vo.MaterialChunkUploadSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 与 {@link icu.jiapeng.kitty.material.upload.controller.MaterialChunkUploadController} 同逻辑；
 * 与 Controller 通过 profile {@code kitty-mam-embedded-api} 互斥。
 */
@Primary
@Service
@RequiredArgsConstructor
@Validated
public class MaterialChunkUploadApiProvider implements MaterialChunkUploadApi {

    private final MaterialChunkUploadService chunkUploadService;

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_CREATE)
    public MaterialChunkUploadSessionVO createSession(MaterialChunkUploadSessionCreateDTO req) {
        return chunkUploadService.createSession(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public MaterialChunkUploadSessionVO getSession(String sessionId) {
        return chunkUploadService.getSession(sessionId);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE)
    public MaterialChunkUploadSessionVO reportPart(String sessionId, MaterialChunkUploadPartReportDTO req) {
        return chunkUploadService.reportPart(sessionId, req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE)
    public MaterialChunkUploadSessionVO completeSession(String sessionId) {
        return chunkUploadService.completeSession(sessionId);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE)
    public MaterialChunkUploadSessionVO cancelSession(String sessionId) {
        return chunkUploadService.cancelSession(sessionId);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE)
    public MaterialChunkUploadSessionVO uploadChunkWithHttpHeaders(
            String sessionId,
            String contentLength,
            String contentRange,
            byte[] body) {
        return chunkUploadService.uploadChunkWithHttpHeaders(sessionId, contentLength, contentRange, body);
    }
}
