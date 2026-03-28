package icu.jiapeng.kitty.material.upload.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.catalog.service.CatalogService;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import icu.jiapeng.kitty.material.resource.constants.ResourceTypeEnum;
import icu.jiapeng.kitty.material.resource.entity.KtResource;
import icu.jiapeng.kitty.material.resource.mapper.KtResourceMapper;
import icu.jiapeng.kitty.material.support.lock.RedissonDistributedLockOperator;
import icu.jiapeng.kitty.material.upload.ChunkUploadSessionCreateSpec;
import icu.jiapeng.kitty.material.upload.ChunkUploadSessionStatus;
import icu.jiapeng.kitty.material.upload.chunk.ChunkStagingPort;
import icu.jiapeng.kitty.material.upload.dto.MaterialChunkUploadPartReportDTO;
import icu.jiapeng.kitty.material.upload.dto.MaterialChunkUploadSessionCreateDTO;
import icu.jiapeng.kitty.material.upload.dto.MaterialPrecatalogPayloadDTO;
import icu.jiapeng.kitty.material.upload.entity.KtChunkUploadPart;
import icu.jiapeng.kitty.material.upload.entity.KtChunkUploadSession;
import icu.jiapeng.kitty.material.upload.mapper.KtChunkUploadPartMapper;
import icu.jiapeng.kitty.material.upload.mapper.KtChunkUploadSessionMapper;
import icu.jiapeng.kitty.material.upload.support.ChunkUploadHttpHeadersSupport;
import icu.jiapeng.kitty.material.upload.support.ContentByteRange;
import icu.jiapeng.kitty.material.upload.vo.MaterialChunkUploadPartVO;
import icu.jiapeng.kitty.material.upload.vo.MaterialChunkUploadSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MaterialChunkUploadService {

    private static final long CHUNK_LOCK_WAIT_MS = 8000L;
    private static final long CHUNK_LOCK_LEASE_SECONDS = 30L;
    private static final long FINALIZE_LOCK_WAIT_MS = 30_000L;
    private static final long FINALIZE_LOCK_LEASE_SECONDS = 120L;

    private final ChunkUploadSessionService chunkUploadSessionService;
    private final KtChunkUploadSessionMapper sessionMapper;
    private final KtChunkUploadPartMapper partMapper;
    private final RedissonDistributedLockOperator distributedLockOperator;
    private final ChunkStagingPort chunkStagingPort;
    private final ChunkUploadMergeService chunkUploadMergeService;
    private final KtResourceMapper resourceMapper;
    private final CatalogService catalogService;

    /**
     * 创建分块上传会话
     *
     * @param req 创建请求
     * @return 创建的分块上传会话VO
     */
    public MaterialChunkUploadSessionVO createSession(MaterialChunkUploadSessionCreateDTO req) {
        if (req == null || req.getTotalSize() == null || req.getChunkSize() == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (!StringUtils.hasText(req.getResourceId())) {
            validateMamSessionCreate(req);
            catalogService.requireOnCatalog(req.getCatalogId().trim(), MaterialPermissionCode.MATERIAL_RESOURCE_CREATE);
        } else {
            guardResourceCatalogForChunk(req.getResourceId().trim(), MaterialPermissionCode.MATERIAL_RESOURCE_CREATE);
        }
        if (req.getPrecatalog() != null) {
            String catalogForPrecatalog;
            if (!StringUtils.hasText(req.getResourceId())) {
                catalogForPrecatalog = req.getCatalogId().trim();
            } else {
                KtResource resource = resourceMapper.selectById(req.getResourceId().trim());
                if (resource == null) {
                    throw BizException.of(ResultStatus.PARAM_ERROR);
                }
                catalogForPrecatalog = resource.getCatalogId();
            }
            catalogService.requireOnCatalog(catalogForPrecatalog, MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE);
        }
        KtChunkUploadSession created = chunkUploadSessionService.create(toCreateSpec(req));
        return toVo(created, List.of());
    }

    private void validateMamSessionCreate(MaterialChunkUploadSessionCreateDTO req) {
        if (!StringUtils.hasText(req.getCatalogId()) || !StringUtils.hasText(req.getTitle()) || req.getType() == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (ResourceTypeEnum.ofType(req.getType()).isEmpty() || ResourceTypeEnum.isFolder(req.getType())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        int chunkCount = ChunkUploadSessionService.computeChunkCount(req.getTotalSize(), req.getChunkSize());
        List<Long> crcs = req.getChunkCrc32List();
        if (chunkCount == 0) {
            if (req.getTotalSize() != 0) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            if (crcs != null && !crcs.isEmpty()) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
        } else if (crcs == null || crcs.size() != chunkCount) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
    }

    private ChunkUploadSessionCreateSpec toCreateSpec(MaterialChunkUploadSessionCreateDTO req) {
        ChunkUploadSessionCreateSpec spec = new ChunkUploadSessionCreateSpec();
        spec.setStorageId(req.getStorageId());
        spec.setObjectKey(req.getObjectKey());
        spec.setTotalSize(req.getTotalSize());
        spec.setChunkSize(req.getChunkSize());
        spec.setPrecatalogJson(serializePrecatalog(req.getPrecatalog()));
        if (StringUtils.hasText(req.getResourceId())) {
            spec.setExistingResourceId(req.getResourceId().trim());
        } else {
            spec.setCatalogId(req.getCatalogId().trim());
            spec.setParentResourceId(req.getParentId());
            spec.setTitle(req.getTitle().trim());
            spec.setResourceType(req.getType());
            spec.setChunkCrc32Unsigned(req.getChunkCrc32List());
        }
        return spec;
    }

    private String serializePrecatalog(MaterialPrecatalogPayloadDTO p) {
        if (p == null) {
            return null;
        }
        if (!StringUtils.hasText(p.getTemplateId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        try {
            JSONObject root = new JSONObject();
            root.put("templateId", p.getTemplateId());
            root.put("fieldValues", p.getFieldValues() == null ? Map.of() : p.getFieldValues());
            return root.toJSONString();
        } catch (Exception e) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
    }

    /**
     * 获取分块上传会话
     *
     * @param sessionId 会话ID
     * @return 分块上传会话VO
     */
    public MaterialChunkUploadSessionVO getSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtChunkUploadSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        guardResourceCatalogForChunk(session.getResourceId(), MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW);
        List<KtChunkUploadPart> parts = partMapper.selectList(
                new QueryWrapper<KtChunkUploadPart>().eq("session_id", sessionId)
        );
        return toVo(session, parts);
    }

    /**
     * 报告分块上传部分
     *
     * @param sessionId 会话ID
     * @param req       报告请求
     * @return 分块上传会话VO
     */
    public MaterialChunkUploadSessionVO reportPart(String sessionId, MaterialChunkUploadPartReportDTO req) {
        if (req == null || req.getChunkIndex() == null || req.getByteSize() == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        guardChunkSessionMutate(sessionId);
        registerPartUnderChunkLock(sessionId, req.getChunkIndex(), req.getByteSize(), null);
        return toVo(sessionId);
    }

    /**
     * 完成分块上传会话
     *
     * @param sessionId 会话ID
     * @return 分块上传会话VO
     */
    public MaterialChunkUploadSessionVO completeSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        guardChunkSessionMutate(sessionId);
        distributedLockOperator.executeWithLock(
                "material:upload:finalize:" + sessionId,
                FINALIZE_LOCK_WAIT_MS,
                FINALIZE_LOCK_LEASE_SECONDS,
                () -> chunkUploadMergeService.mergeDiskVerifyAndComplete(sessionId));
        return toVo(sessionId);
    }

    /**
     * 取消分块上传会话
     *
     * @param sessionId 会话ID
     * @return 分块上传会话VO
     */
    public MaterialChunkUploadSessionVO cancelSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        guardChunkSessionMutate(sessionId);
        chunkUploadSessionService.cancel(sessionId);
        if (StringUtils.hasText(sessionId)) {
            chunkStagingPort.deleteSession(sessionId);
        }
        return toVo(sessionId);
    }

    /**
     * 使用HTTP头上传分块
     *
     * @param sessionId           会话ID
     * @param contentLengthHeader Content-Length头
     * @param contentRangeHeader  Content-Range头
     * @param body                分块数据
     * @return 分块上传会话VO
     */
    public MaterialChunkUploadSessionVO uploadChunkWithHttpHeaders(String sessionId, String contentLengthHeader, String contentRangeHeader, byte[] body) {
        if (!StringUtils.hasText(sessionId) || !StringUtils.hasText(contentRangeHeader)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        byte[] payload = body == null ? new byte[0] : body;
        long contentLength;
        if (!StringUtils.hasText(contentLengthHeader)) {
            contentLength = payload.length;
        } else {
            contentLength = ChunkUploadHttpHeadersSupport.parseContentLengthLong(contentLengthHeader);
            if (contentLength != payload.length) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
        }
        ContentByteRange range = ChunkUploadHttpHeadersSupport.parseContentRangeBytes(contentRangeHeader);
        ChunkUploadHttpHeadersSupport.validateRangeContentLengthAndBody(range, contentLength, payload.length);

        KtChunkUploadSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (!ChunkUploadSessionStatus.UPLOADING.equals(session.getStatus())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (session.getChunkCount() == 0) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (session.getTotalSize() != range.completeLength()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        guardChunkSessionMutate(sessionId);
        int chunkIndex = chunkUploadSessionService.resolveChunkIndexForByteRange(session, range.firstBytePos(), range.lastBytePos());
        registerPartUnderChunkLock(sessionId, chunkIndex, contentLength, payload);
        return toVo(sessionId);
    }

    private void guardChunkSessionMutate(String sessionId) {
        KtChunkUploadSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        guardResourceCatalogForChunk(session.getResourceId(), MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE);
    }

    private void guardResourceCatalogForChunk(String resourceId, String permissionCode) {
        KtResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        catalogService.requireOnCatalog(resource.getCatalogId(), permissionCode);
    }

    private void registerPartUnderChunkLock(String sessionId, int chunkIndex, long byteSize, byte[] stagingPayloadOrNull) {
        String lockKey = "material:upload:chunk:" + sessionId + ":" + chunkIndex;
        distributedLockOperator.executeWithLock(lockKey, CHUNK_LOCK_WAIT_MS, CHUNK_LOCK_LEASE_SECONDS, () -> {
            if (stagingPayloadOrNull != null) {
                chunkStagingPort.writePart(sessionId, chunkIndex, stagingPayloadOrNull);
            }
            chunkUploadSessionService.registerPart(sessionId, chunkIndex, byteSize);
        });
    }

    private MaterialChunkUploadSessionVO toVo(String sessionId) {
        KtChunkUploadSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        List<KtChunkUploadPart> parts = partMapper.selectList(
                new QueryWrapper<KtChunkUploadPart>().eq("session_id", sessionId)
        );
        return toVo(session, parts);
    }

    private MaterialChunkUploadSessionVO toVo(KtChunkUploadSession session, List<KtChunkUploadPart> parts) {
        MaterialChunkUploadSessionVO vo = new MaterialChunkUploadSessionVO();
        vo.setId(session.getId());
        vo.setResourceId(session.getResourceId());
        vo.setCatalogId(session.getCatalogId());
        vo.setParentResourceId(session.getParentResourceId());
        vo.setTitle(session.getTitle());
        vo.setResourceType(session.getResourceType());
        vo.setStorageId(session.getStorageId());
        vo.setObjectKey(session.getObjectKey());
        vo.setTotalSize(session.getTotalSize());
        vo.setChunkSize(session.getChunkSize());
        vo.setChunkCount(session.getChunkCount());
        vo.setStatus(session.getStatus());
        vo.setParts(parts.stream().map(this::toPartVo).toList());
        return vo;
    }

    private MaterialChunkUploadPartVO toPartVo(KtChunkUploadPart p) {
        MaterialChunkUploadPartVO vo = new MaterialChunkUploadPartVO();
        vo.setChunkIndex(p.getChunkIndex());
        vo.setByteSize(p.getByteSize());
        return vo;
    }
}
