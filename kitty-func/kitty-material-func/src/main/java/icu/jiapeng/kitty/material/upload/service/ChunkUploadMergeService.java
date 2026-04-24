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
import icu.jiapeng.kitty.material.transcode.service.MaterialTranscodeVideoFollowUpService;
import icu.jiapeng.kitty.material.storage.StorageDriver;
import icu.jiapeng.kitty.material.storage.StorageDriverFactory;
import icu.jiapeng.kitty.material.storage.StorageMimeTypes;
import icu.jiapeng.kitty.material.storage.StoragePartEtag;
import icu.jiapeng.kitty.material.upload.ChunkUploadSessionStatus;
import icu.jiapeng.kitty.material.upload.chunk.ChunkStagingPort;
import icu.jiapeng.kitty.material.upload.entity.KtChunkUploadPart;
import icu.jiapeng.kitty.material.upload.entity.KtChunkUploadSession;
import icu.jiapeng.kitty.material.upload.entity.chunk.FilesystemChunkStagingService;
import icu.jiapeng.kitty.material.upload.mapper.KtChunkUploadPartMapper;
import icu.jiapeng.kitty.material.upload.mapper.KtChunkUploadSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final FilesystemChunkStagingService diskChunkStaging;
    private final KtFileStorageMapper fileStorageMapper;
    private final KtResourceMapper resourceMapper;
    private final MetaFileStorageBindService metaFileStorageBindService;
    private final MaterialResourceTaskService materialResourceTaskService;
    private final MaterialMetadataInstanceService metadataInstanceService;
    private final MaterialSearchSyncTrigger materialSearchSyncTrigger;
    private final StorageDriverFactory storageDriverFactory;
    private final MaterialTranscodeVideoFollowUpService materialTranscodeVideoFollowUpService;

    /**
     * 校验分片与指纹后落最终对象：磁盘引擎顺序拼接本地分片文件；对象存储引擎 CompleteMultipartUpload（无本地合并临时文件）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void mergeVerifyAndComplete(String sessionId) {
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
        StorageDriver driver = storageDriverFactory.resolve(storage.getStorageType());
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
                String contentType = StorageMimeTypes.resolveFromTitleAndObjectKey(session.getTitle(), session.getObjectKey());
                driver.putEmptyObject(storage, session.getObjectKey(), contentType);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            finalizeAfterMergedFile(session);
            return;
        }

        chunkUploadSessionService.flushUploadPartsFromCacheToDb(sessionId);

        List<KtChunkUploadPart> parts = partMapper.selectList(
                new QueryWrapper<KtChunkUploadPart>().eq("session_id", sessionId)
        );
        if (parts.size() != chunkCount) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        parts.sort(Comparator.comparingInt(KtChunkUploadPart::getChunkIndex));

        if (resource.getFingerprint() == null || resource.getFingerprint().isBlank()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }

        if (FileEngineTypeEnum.OBJECT_STORAGE.getType().equals(storage.getStorageType())) {
            mergeObjectStorage(session, storage, driver, parts);
        } else if (FileEngineTypeEnum.DISK.getType().equals(storage.getStorageType())) {
            mergeDisk(session, storage, driver, resource);
        } else {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }

        finalizeAfterMergedFile(session);
    }

    private void mergeObjectStorage(
            KtChunkUploadSession session,
            KtFileStorage storage,
            StorageDriver driver,
            List<KtChunkUploadPart> parts) {
        if (!StringUtils.hasText(session.getMultipartUploadId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        int chunkCount = session.getChunkCount();
        List<StoragePartEtag> completed = new ArrayList<>(chunkCount);
        for (int i = 0; i < chunkCount; i++) {
            KtChunkUploadPart p = parts.get(i);
            if (p.getChunkIndex() != i) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            long expected = chunkUploadSessionService.expectedChunkByteSize(session, i);
            if (!p.getByteSize().equals(expected)) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            if (!StringUtils.hasText(p.getPartEtag())) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            completed.add(new StoragePartEtag(i + 1, p.getPartEtag()));
        }
        try {
            driver.completeMultipartUpload(storage, session.getObjectKey(), session.getMultipartUploadId(), completed);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void mergeDisk(
            KtChunkUploadSession session,
            KtFileStorage storage,
            StorageDriver driver,
            KtResource resource) {
        String sessionId = session.getId();
        int chunkCount = session.getChunkCount();
        List<Long> crcs = new ArrayList<>(chunkCount);
        try {
            for (int i = 0; i < chunkCount; i++) {
                if (!diskChunkStaging.partExists(sessionId, i)) {
                    throw BizException.of(ResultStatus.PARAM_ERROR);
                }
                long expected = chunkUploadSessionService.expectedChunkByteSize(session, i);
                if (diskChunkStaging.partByteSize(sessionId, i) != expected) {
                    throw BizException.of(ResultStatus.PARAM_ERROR);
                }
                Path partPath = diskChunkStaging.resolvePartPath(sessionId, i);
                crcs.add(ResourceFingerprintSupport.crc32Unsigned(partPath));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        String computed = ResourceFingerprintSupport.format(session.getTotalSize(), crcs);
        if (!computed.equals(resource.getFingerprint())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }

        List<Path> ordered = new ArrayList<>(chunkCount);
        try {
            for (int i = 0; i < chunkCount; i++) {
                ordered.add(diskChunkStaging.resolvePartPath(sessionId, i));
            }
            driver.writeSequentialLocalPartFilesToObject(storage, session.getObjectKey(), ordered);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void finalizeAfterMergedFile(KtChunkUploadSession session) {
        String sessionId = session.getId();
        metaFileStorageBindService.bind(session.getResourceId(), session.getStorageId(), session.getObjectKey(), null);
        KtResource resource = resourceMapper.selectById(session.getResourceId());
        if (resource != null) {
            materialResourceTaskService.onUploadFileBound(resource, session.getTranscodeStrategyId());
            materialSearchSyncTrigger.publishFullDocument(resource);
            final KtResource afterBind = resource;
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        materialTranscodeVideoFollowUpService.scheduleIfVideo(afterBind);
                    }
                });
            } else {
                materialTranscodeVideoFollowUpService.scheduleIfVideo(afterBind);
            }
        }
        chunkUploadSessionService.complete(sessionId);
        chunkStagingPort.deleteSession(sessionId);
        applyPrecatalogIfPresent(session);
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
}
