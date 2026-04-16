package icu.jiapeng.kitty.material.upload.entity.chunk;

import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.resource.constants.FileEngineTypeEnum;
import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;
import icu.jiapeng.kitty.material.resource.mapper.KtFileStorageMapper;
import icu.jiapeng.kitty.material.storage.StorageDriver;
import icu.jiapeng.kitty.material.storage.StorageDriverFactory;
import icu.jiapeng.kitty.material.upload.ChunkUploadSessionStatus;
import icu.jiapeng.kitty.material.upload.chunk.ChunkStagingPort;
import icu.jiapeng.kitty.material.upload.cache.ChunkUploadSessionHotCache;
import icu.jiapeng.kitty.material.upload.entity.KtChunkUploadSession;
import icu.jiapeng.kitty.material.upload.mapper.KtChunkUploadSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * 按存储引擎路由：磁盘写本地 staging；对象存储直传 S3 UploadPart。
 */
@Service
@Primary
@RequiredArgsConstructor
public class RoutingChunkStagingService implements ChunkStagingPort {

    private final FilesystemChunkStagingService filesystemChunkStaging;
    private final KtFileStorageMapper fileStorageMapper;
    private final KtChunkUploadSessionMapper sessionMapper;
    private final StorageDriverFactory storageDriverFactory;
    private final ChunkUploadSessionHotCache chunkUploadSessionHotCache;

    @Override
    public String writePart(String sessionId, int chunkIndex, byte[] data) {
        if (data == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtChunkUploadSession session = loadSession(sessionId);
        if (!ChunkUploadSessionStatus.UPLOADING.equals(session.getStatus())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtFileStorage storage = fileStorageMapper.selectById(session.getStorageId());
        if (storage == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (FileEngineTypeEnum.DISK.getType().equals(storage.getStorageType())) {
            filesystemChunkStaging.writePart(sessionId, chunkIndex, data);
            return null;
        }
        if (FileEngineTypeEnum.OBJECT_STORAGE.getType().equals(storage.getStorageType())) {
            String uploadId = session.getMultipartUploadId();
            if (!StringUtils.hasText(uploadId)) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            StorageDriver driver = storageDriverFactory.resolve(storage.getStorageType());
            try {
                return driver.uploadMultipartPart(storage, session.getObjectKey(), uploadId, chunkIndex + 1, data);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        throw BizException.of(ResultStatus.PARAM_ERROR);
    }

    @Override
    public boolean partExists(String sessionId, int chunkIndex) {
        return requireDiskStaging(sessionId).partExists(sessionId, chunkIndex);
    }

    @Override
    public long partByteSize(String sessionId, int chunkIndex) {
        return requireDiskStaging(sessionId).partByteSize(sessionId, chunkIndex);
    }

    @Override
    public Path resolvePartPath(String sessionId, int chunkIndex) {
        return requireDiskStaging(sessionId).resolvePartPath(sessionId, chunkIndex);
    }

    @Override
    public void deleteSession(String sessionId) {
        KtChunkUploadSession session = loadSession(sessionId);
        KtFileStorage storage = fileStorageMapper.selectById(session.getStorageId());
        if (storage != null && FileEngineTypeEnum.DISK.getType().equals(storage.getStorageType())) {
            filesystemChunkStaging.deleteSession(sessionId);
        }
    }

    private FilesystemChunkStagingService requireDiskStaging(String sessionId) {
        KtChunkUploadSession s = loadSession(sessionId);
        KtFileStorage st = fileStorageMapper.selectById(s.getStorageId());
        if (st == null || !FileEngineTypeEnum.DISK.getType().equals(st.getStorageType())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        return filesystemChunkStaging;
    }

    private KtChunkUploadSession loadSession(String sessionId) {
        KtChunkUploadSession s = chunkUploadSessionHotCache.getSessionSnapshot(sessionId);
        if (s == null) {
            s = sessionMapper.selectById(sessionId);
        }
        if (s == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        return s;
    }
}
