package icu.jiapeng.kitty.material.upload.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataSaveDTO;
import icu.jiapeng.kitty.material.metadata.service.MaterialMetadataInstanceService;
import icu.jiapeng.kitty.material.resource.constants.FileEngineTypeEnum;
import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;
import icu.jiapeng.kitty.material.resource.entity.KtResource;
import icu.jiapeng.kitty.material.resource.fingerprint.ResourceFingerprintSupport;
import icu.jiapeng.kitty.material.resource.mapper.KtFileStorageMapper;
import icu.jiapeng.kitty.material.resource.mapper.KtResourceMapper;
import icu.jiapeng.kitty.material.resource.service.MetaFileStorageBindService;
import icu.jiapeng.kitty.material.searchsync.service.MaterialSearchSyncTrigger;
import icu.jiapeng.kitty.material.task.service.MaterialResourceTaskService;
import icu.jiapeng.kitty.material.upload.ChunkUploadSessionStatus;
import icu.jiapeng.kitty.material.upload.chunk.ChunkStagingPort;
import icu.jiapeng.kitty.material.upload.entity.KtChunkUploadPart;
import icu.jiapeng.kitty.material.upload.entity.KtChunkUploadSession;
import icu.jiapeng.kitty.material.upload.mapper.KtChunkUploadPartMapper;
import icu.jiapeng.kitty.material.upload.mapper.KtChunkUploadSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkUploadMergeService {

    private final KtChunkUploadSessionMapper sessionMapper;
    private final KtChunkUploadPartMapper partMapper;
    private final ChunkUploadSessionService chunkUploadSessionService;
    private final ChunkStagingPort chunkStagingPort;
    private final KtFileStorageMapper fileStorageMapper;
    private final KtResourceMapper resourceMapper;
    private final MetaFileStorageBindService metaFileStorageBindService;
    private final MaterialResourceTaskService materialResourceTaskService;
    private final MaterialMetadataInstanceService metadataInstanceService;
    private final MaterialSearchSyncTrigger materialSearchSyncTrigger;

    @Transactional(rollbackFor = Exception.class)
    public void mergeDiskVerifyAndComplete(String sessionId) {
        KtChunkUploadSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (!ChunkUploadSessionStatus.UPLOADING.equals(session.getStatus())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtFileStorage storage = fileStorageMapper.selectById(session.getStorageId());
        if (storage == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (!FileEngineTypeEnum.DISK.getType().equals(storage.getStorageType())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = resourceMapper.selectById(session.getResourceId());
        if (resource == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }

        int chunkCount = session.getChunkCount();
        if (chunkCount == 0) {
            if (session.getTotalSize() != 0) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            try {
                Path target = diskTarget(storage.getBucket(), session.getObjectKey());
                Files.createDirectories(target.getParent());
                Files.write(target, new byte[0]);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            finalizeAfterMergedFileOnDisk(session);
            return;
        }

        List<KtChunkUploadPart> parts = partMapper.selectList(
                new QueryWrapper<KtChunkUploadPart>().eq("session_id", sessionId)
        );
        if (parts.size() != chunkCount) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }

        if (resource.getFingerprint() == null || resource.getFingerprint().isBlank()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }

        List<Long> crcs = new ArrayList<>(chunkCount);
        try {
            for (int i = 0; i < chunkCount; i++) {
                if (!chunkStagingPort.partExists(sessionId, i)) {
                    throw BizException.of(ResultStatus.PARAM_ERROR);
                }
                long expected = chunkUploadSessionService.expectedChunkByteSize(session, i);
                if (chunkStagingPort.partByteSize(sessionId, i) != expected) {
                    throw BizException.of(ResultStatus.PARAM_ERROR);
                }
                Path partPath = chunkStagingPort.resolvePartPath(sessionId, i);
                crcs.add(ResourceFingerprintSupport.crc32Unsigned(partPath));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        String computed = ResourceFingerprintSupport.format(session.getTotalSize(), crcs);
        if (!computed.equals(resource.getFingerprint())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }

        try {
            Path target = diskTarget(storage.getBucket(), session.getObjectKey());
            Files.createDirectories(target.getParent());
            try (OutputStream out = Files.newOutputStream(target)) {
                for (int i = 0; i < chunkCount; i++) {
                    Path partPath = chunkStagingPort.resolvePartPath(sessionId, i);
                    Files.copy(partPath, out);
                }
            }
            if (Files.size(target) != session.getTotalSize()) {
                throw BizException.of(ResultStatus.NORMAL_ERROR);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        finalizeAfterMergedFileOnDisk(session);
    }

    private void finalizeAfterMergedFileOnDisk(KtChunkUploadSession session) {
        String sessionId = session.getId();
        metaFileStorageBindService.bind(session.getResourceId(), session.getStorageId(), session.getObjectKey(), null);
        KtResource resource = resourceMapper.selectById(session.getResourceId());
        if (resource != null) {
            materialSearchSyncTrigger.publishFullDocument(resource);
        }
        chunkUploadSessionService.complete(sessionId);
        chunkStagingPort.deleteSession(sessionId);
        applyPrecatalogIfPresent(session);
        materialResourceTaskService.tryAutoEnqueueAfterBind(session.getResourceId());
    }

    private void applyPrecatalogIfPresent(KtChunkUploadSession session) {
        if (session.getPrecatalogJson() == null || session.getPrecatalogJson().isBlank()) {
            return;
        }
        try {
            JSONObject root = JSON.parseObject(session.getPrecatalogJson());
            if (root == null) {
                return;
            }
            String templateId = root.getString("templateId");
            if (templateId == null || templateId.isBlank()) {
                return;
            }
            JSONObject fvObj = root.getJSONObject("fieldValues");
            Map<String, String> fieldValues = fvObj == null || fvObj.isEmpty()
                    ? Map.of()
                    : JSON.parseObject(fvObj.toJSONString(), new TypeReference<>() {
            });
            MaterialMetadataSaveDTO dto = new MaterialMetadataSaveDTO();
            dto.setResourceId(session.getResourceId());
            dto.setTemplateId(templateId);
            dto.setFieldValues(fieldValues);
            metadataInstanceService.save(dto);
        } catch (Exception ex) {
            log.warn("precatalog skipped after chunk merge, sessionId={}", session.getId(), ex);
        }
    }

    private Path diskTarget(String storageMount, String objectKey) {
        if (storageMount == null || storageMount.isBlank()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        Path t = Path.of(storageMount.trim());
        if (objectKey != null) {
            for (String seg : objectKey.split("/")) {
                if (!seg.isEmpty()) {
                    t = t.resolve(seg);
                }
            }
        }
        return t;
    }
}
