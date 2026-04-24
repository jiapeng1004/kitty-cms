package icu.jiapeng.kitty.material.upload.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.resource.constants.FileEngineTypeEnum;
import icu.jiapeng.kitty.material.resource.constants.ResourceTypeEnum;
import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;
import icu.jiapeng.kitty.material.resource.entity.KtResource;
import icu.jiapeng.kitty.material.resource.fingerprint.ResourceFingerprintSupport;
import icu.jiapeng.kitty.material.resource.mapper.KtFileStorageMapper;
import icu.jiapeng.kitty.material.storage.service.KtFileStorageService;
import icu.jiapeng.kitty.material.resource.mapper.KtResourceMapper;
import icu.jiapeng.kitty.material.resource.service.MaterialResourceService;
import icu.jiapeng.kitty.material.storage.StorageDriver;
import icu.jiapeng.kitty.material.storage.StorageDriverFactory;
import icu.jiapeng.kitty.material.storage.StorageMimeTypes;
import icu.jiapeng.kitty.material.upload.ChunkUploadSessionCreateSpec;
import icu.jiapeng.kitty.material.upload.ChunkUploadSessionStatus;
import icu.jiapeng.kitty.material.upload.entity.KtChunkUploadPart;
import icu.jiapeng.kitty.material.upload.entity.KtChunkUploadSession;
import icu.jiapeng.kitty.material.upload.cache.ChunkUploadSessionHotCache;
import icu.jiapeng.kitty.material.upload.mapper.KtChunkUploadPartMapper;
import icu.jiapeng.kitty.material.upload.mapper.KtChunkUploadSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkUploadSessionServiceImpl extends ServiceImpl<KtChunkUploadSessionMapper, KtChunkUploadSession> implements ChunkUploadSessionService {

    private static final String ROOT_PARENT_ID = "0";

    private final KtResourceMapper resourceMapper;
    private final KtFileStorageMapper fileStorageMapper;
    private final KtFileStorageService ktFileStorageService;
    private final MaterialResourceService resourceFolderService;
    private final KtChunkUploadPartMapper partMapper;
    private final StorageDriverFactory storageDriverFactory;
    private final ChunkUploadSessionHotCache chunkUploadSessionHotCache;

    /**
     * 创建分块上传会话
     *
     * @param spec 创建规格
     * @return 创建的分块上传会话
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public KtChunkUploadSession create(ChunkUploadSessionCreateSpec spec) {
        if (spec == null || !StringUtils.hasText(spec.getObjectKey())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String storageId = spec.getStorageId();
        if (!StringUtils.hasText(storageId)) {
            storageId = ktFileStorageService.requirePrimaryStorageId();
            spec.setStorageId(storageId);
        }
        long totalSize = spec.getTotalSize();
        long chunkSize = spec.getChunkSize();
        if (totalSize < 0 || chunkSize <= 0) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtFileStorage storage = fileStorageMapper.selectById(spec.getStorageId());
        if (storage == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        StorageDriver driver = storageDriverFactory.resolve(storage.getStorageType());
        String normalizedKey = driver.normalizeObjectKey(spec.getObjectKey());
        if (!driver.isValidObjectKey(normalizedKey)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        int chunkCount = ChunkUploadSessionService.computeChunkCount(totalSize, chunkSize);
        KtResource resource;

        KtChunkUploadSession session = new KtChunkUploadSession();
        session.setId(UUID.randomUUID().toString());
        session.setStorageId(spec.getStorageId());
        session.setObjectKey(normalizedKey);
        session.setTotalSize(totalSize);
        session.setChunkSize(chunkSize);
        session.setChunkCount(chunkCount);
        session.setStatus(ChunkUploadSessionStatus.UPLOADING);
        session.setPrecatalogJson(spec.getPrecatalogJson());
        if (StringUtils.hasText(spec.getTranscodeStrategyId())) {
            session.setTranscodeStrategyId(spec.getTranscodeStrategyId().trim());
        }

        if (StringUtils.hasText(spec.getExistingResourceId())) {
            resource = resourceMapper.selectById(spec.getExistingResourceId().trim());
            if (resource == null) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            if (ResourceTypeEnum.isFolder(resource.getType())) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            session.setResourceId(resource.getId());
            session.setCatalogId(resource.getCatalogId());
            session.setParentResourceId(resource.getParentId());
            session.setTitle(resource.getTitle());
            session.setResourceType(resource.getType());
        } else {
            resource = createResourceStubForMamChunkSession(spec);
            session.setResourceId(resource.getId());
            session.setCatalogId(resource.getCatalogId());
            session.setParentResourceId(resource.getParentId());
            session.setTitle(resource.getTitle());
            session.setResourceType(resource.getType());
        }

        save(session);
        if (chunkCount > 0 && FileEngineTypeEnum.OBJECT_STORAGE.getType().equals(storage.getStorageType())) {
            try {
                String contentType = StorageMimeTypes.resolveFromTitleAndObjectKey(session.getTitle(), normalizedKey);
                String uploadId = driver.initiateMultipartUpload(storage, normalizedKey, contentType);
                session.setMultipartUploadId(uploadId);
                updateById(session);
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        }
        chunkUploadSessionHotCache.putSessionSnapshot(session);
        return session;
    }

    private KtResource createResourceStubForMamChunkSession(ChunkUploadSessionCreateSpec spec) {
        if (!StringUtils.hasText(spec.getCatalogId()) || !StringUtils.hasText(spec.getTitle()) || spec.getResourceType() == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (ResourceTypeEnum.ofType(spec.getResourceType()).isEmpty() || ResourceTypeEnum.isFolder(spec.getResourceType())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        int chunkCount = ChunkUploadSessionService.computeChunkCount(spec.getTotalSize(), spec.getChunkSize());
        List<Long> crcs = spec.getChunkCrc32Unsigned();
        if (chunkCount == 0) {
            if (spec.getTotalSize() != 0) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            if (crcs != null && !crcs.isEmpty()) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
        } else {
            if (crcs == null || crcs.size() != chunkCount) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
        }

        String parentId = normalizeParentId(spec.getParentResourceId());
        if (!ROOT_PARENT_ID.equals(parentId)) {
            KtResource parent = resourceMapper.selectById(parentId);
            if (parent == null) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            if (!ResourceTypeEnum.isFolder(parent.getType())) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            if (parent.getCatalogId() == null || !parent.getCatalogId().equals(spec.getCatalogId())) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
        }

        KtResource r = new KtResource();
        r.setId(UUID.randomUUID().toString());
        r.setTitle(spec.getTitle().trim());
        r.setCatalogId(spec.getCatalogId().trim());
        r.setParentId(parentId);
        r.setType(spec.getResourceType());
        r.setFileSize(spec.getTotalSize());
        if (chunkCount == 0) {
            r.setFingerprint(ResourceFingerprintSupport.format(0L, List.of()));
        } else {
            List<Long> crcNonNull = Objects.requireNonNull(crcs);
            r.setFingerprint(ResourceFingerprintSupport.format(spec.getTotalSize(), crcNonNull));
        }
        resourceMapper.insert(r);
        return resourceMapper.selectById(r.getId());
    }

    private String normalizeParentId(String parentId) {
        return !StringUtils.hasText(parentId) ? ROOT_PARENT_ID : parentId.trim();
    }

    /**
     * 注册分块上传部分
     *
     * @param sessionId  会话ID
     * @param chunkIndex 分块索引
     * @param byteSize   字节大小
     * @param partEtag   对象存储分片 ETag（磁盘引擎可为 null）
     */
    @Override
    public void registerPart(String sessionId, int chunkIndex, long byteSize, String partEtag) {
        KtChunkUploadSession session = chunkUploadSessionHotCache.getSessionSnapshot(sessionId);
        if (session == null) {
            session = getById(sessionId);
            if (session != null) {
                chunkUploadSessionHotCache.putSessionSnapshot(session);
            }
        }
        if (session == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (!ChunkUploadSessionStatus.UPLOADING.equals(session.getStatus())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        int count = session.getChunkCount();
        if (count == 0) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (chunkIndex < 0 || chunkIndex >= count) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        long expected = expectedPartSize(session, chunkIndex);
        if (byteSize != expected) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }

        KtFileStorage storage = fileStorageMapper.selectById(session.getStorageId());
        if (storage == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        boolean objectStorage = FileEngineTypeEnum.OBJECT_STORAGE.getType().equals(storage.getStorageType());

        if (objectStorage && (partEtag == null || partEtag.isBlank())) {
            String entry = chunkUploadSessionHotCache.getPartEntry(sessionId, chunkIndex);
            if (entry != null && entry.contains("|")) {
                long sz = ChunkUploadSessionHotCache.parseByteSizeFromPartEntry(entry);
                if (sz == byteSize) {
                    return;
                }
            }
            KtChunkUploadPart dbPart = partMapper.selectOne(
                    new QueryWrapper<KtChunkUploadPart>()
                            .eq("session_id", sessionId)
                            .eq("chunk_index", chunkIndex)
            );
            if (dbPart != null && StringUtils.hasText(dbPart.getPartEtag()) && dbPart.getByteSize().equals(byteSize)) {
                return;
            }
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }

        String redisEntry = chunkUploadSessionHotCache.getPartEntry(sessionId, chunkIndex);
        if (redisEntry != null) {
            long existingSz = ChunkUploadSessionHotCache.parseByteSizeFromPartEntry(redisEntry);
            if (existingSz != byteSize) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            if (objectStorage) {
                String existingEtag = ChunkUploadSessionHotCache.parseEtagFromPartEntry(redisEntry);
                if (StringUtils.hasText(partEtag) && StringUtils.hasText(existingEtag) && existingEtag.equals(partEtag)) {
                    return;
                }
                if (StringUtils.hasText(existingEtag) && StringUtils.hasText(partEtag) && !existingEtag.equals(partEtag)) {
                    throw BizException.of(ResultStatus.PARAM_ERROR);
                }
            } else {
                return;
            }
        }

        KtChunkUploadPart existing = partMapper.selectOne(
                new QueryWrapper<KtChunkUploadPart>()
                        .eq("session_id", sessionId)
                        .eq("chunk_index", chunkIndex)
        );

        if (existing != null) {
            if (!existing.getByteSize().equals(byteSize)) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            if (objectStorage && StringUtils.hasText(existing.getPartEtag()) && StringUtils.hasText(partEtag)
                    && !existing.getPartEtag().equals(partEtag)) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            return;
        }

        chunkUploadSessionHotCache.recordPart(sessionId, chunkIndex, byteSize, objectStorage ? partEtag : null);
    }

    @Override
    public void flushUploadPartsFromCacheToDb(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        Map<String, String> entries = chunkUploadSessionHotCache.loadPartEntries(sessionId);
        if (entries.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> e : entries.entrySet()) {
            int idx = Integer.parseInt(e.getKey());
            long sz = ChunkUploadSessionHotCache.parseByteSizeFromPartEntry(e.getValue());
            String etag = ChunkUploadSessionHotCache.parseEtagFromPartEntry(e.getValue());
            KtChunkUploadPart existing = partMapper.selectOne(
                    new QueryWrapper<KtChunkUploadPart>()
                            .eq("session_id", sessionId)
                            .eq("chunk_index", idx)
            );
            if (existing != null) {
                continue;
            }
            KtChunkUploadPart part = new KtChunkUploadPart();
            part.setId(UUID.randomUUID().toString());
            part.setSessionId(sessionId);
            part.setChunkIndex(idx);
            part.setByteSize(sz);
            part.setPartEtag(etag);
            try {
                partMapper.insert(part);
            } catch (DataIntegrityViolationException ex) {
                // 并发完成时可能重复插入，忽略
            }
        }
    }

    /**
     * 根据字节范围解析分块索引
     *
     * @param session            分块上传会话
     * @param firstByteInclusive 起始字节位置（包含）
     * @param lastByteInclusive  结束字节位置（包含）
     * @return 分块索引
     */
    @Override
    public int resolveChunkIndexForByteRange(KtChunkUploadSession session, long firstByteInclusive, long lastByteInclusive) {
        int count = session.getChunkCount();
        if (count == 0) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        for (int i = 0; i < count; i++) {
            long start = (long) i * session.getChunkSize();
            long partLen = expectedPartSize(session, i);
            long end = start + partLen - 1;
            if (firstByteInclusive == start && lastByteInclusive == end) {
                return i;
            }
        }
        throw BizException.of(ResultStatus.PARAM_ERROR);
    }

    /**
     * 完成分块上传
     *
     * @param sessionId 会话ID
     * @return 完成后的分块上传会话
     */
    @Override
    public KtChunkUploadSession complete(String sessionId) {
        KtChunkUploadSession session = getById(sessionId);
        if (session == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (!ChunkUploadSessionStatus.UPLOADING.equals(session.getStatus())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        int count = session.getChunkCount();
        if (count == 0) {
            if (session.getTotalSize() != 0) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            session.setStatus(ChunkUploadSessionStatus.COMPLETED);
            updateById(session);
            chunkUploadSessionHotCache.deleteUploadState(sessionId);
            return session;
        }

        flushUploadPartsFromCacheToDb(sessionId);

        List<KtChunkUploadPart> parts = partMapper.selectList(
                new QueryWrapper<KtChunkUploadPart>().eq("session_id", sessionId)
        );

        if (parts.size() != count) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        Map<Integer, Long> indexToSize = new HashMap<>();
        for (KtChunkUploadPart p : parts) {
            indexToSize.put(p.getChunkIndex(), p.getByteSize());
        }
        for (int i = 0; i < count; i++) {
            Long sz = indexToSize.get(i);
            if (sz == null || sz != expectedPartSize(session, i)) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
        }
        session.setStatus(ChunkUploadSessionStatus.COMPLETED);
        updateById(session);
        chunkUploadSessionHotCache.deleteUploadState(sessionId);
        return session;
    }

    /**
     * 取消分块上传
     *
     * @param sessionId 会话ID
     * @return 取消后的分块上传会话
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public KtChunkUploadSession cancel(String sessionId) {
        KtChunkUploadSession session = getById(sessionId);
        if (session == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (!ChunkUploadSessionStatus.UPLOADING.equals(session.getStatus())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtFileStorage storage = fileStorageMapper.selectById(session.getStorageId());
        if (storage != null
                && FileEngineTypeEnum.OBJECT_STORAGE.getType().equals(storage.getStorageType())
                && StringUtils.hasText(session.getMultipartUploadId())) {
            try {
                StorageDriver driver = storageDriverFactory.resolve(storage.getStorageType());
                driver.abortMultipartUpload(storage, session.getObjectKey(), session.getMultipartUploadId());
            } catch (IOException e) {
                log.warn("abort multipart upload failed sessionId={}", sessionId, e);
            }
        }
        partMapper.delete(new QueryWrapper<KtChunkUploadPart>().eq("session_id", sessionId));
        session.setStatus(ChunkUploadSessionStatus.CANCELLED);
        updateById(session);
        chunkUploadSessionHotCache.deleteUploadState(sessionId);
        return session;
    }

    /**
     * 获取分块的预期字节大小
     *
     * @param session    分块上传会话
     * @param chunkIndex 分块索引
     * @return 预期字节大小
     */
    @Override
    public long expectedChunkByteSize(KtChunkUploadSession session, int chunkIndex) {
        return expectedPartSize(session, chunkIndex);
    }

    private long expectedPartSize(KtChunkUploadSession session, int chunkIndex) {
        int lastIndex = session.getChunkCount() - 1;
        if (chunkIndex < lastIndex) {
            return session.getChunkSize();
        }
        return session.getTotalSize() - (long) lastIndex * session.getChunkSize();
    }
}
