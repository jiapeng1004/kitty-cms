package icu.jiapeng.kitty.material.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.resource.constants.ResourceTypeEnum;
import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;
import icu.jiapeng.kitty.material.resource.entity.KtMetaFile;
import icu.jiapeng.kitty.material.resource.entity.KtResource;
import icu.jiapeng.kitty.material.resource.mapper.KtFileStorageMapper;
import icu.jiapeng.kitty.material.resource.mapper.KtMetaFileMapper;
import icu.jiapeng.kitty.material.resource.mapper.KtResourceMapper;
import icu.jiapeng.kitty.material.storage.StorageDriver;
import icu.jiapeng.kitty.material.storage.StorageDriverFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetaFileStorageBindServiceImpl implements MetaFileStorageBindService {

    private final KtResourceMapper resourceMapper;
    private final KtFileStorageMapper fileStorageMapper;
    private final KtMetaFileMapper metaFileMapper;
    private final StorageDriverFactory storageDriverFactory;

    @Override
    public KtMetaFile bind(String resourceId, String storageId, String objectKey, String overrideName) {
        if (isBlank(resourceId) || isBlank(storageId) || isBlank(objectKey)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (ResourceTypeEnum.isFolder(resource.getType())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtFileStorage storage = fileStorageMapper.selectById(storageId);
        if (storage == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        StorageDriver driver = storageDriverFactory.resolve(storage.getStorageType());
        String normalizedKey = driver.normalizeObjectKey(objectKey);
        if (!driver.isValidObjectKey(normalizedKey)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtMetaFile meta = metaFileMapper.selectOne(
                new QueryWrapper<KtMetaFile>().eq("resource_id", resourceId)
        );
        if (meta == null) {
            meta = new KtMetaFile();
        }
        meta.setResourceId(resourceId);
        meta.setStorageId(storageId);
        meta.setObjectKey(normalizedKey);
        String fallbackTitle = resource.getTitle() != null && !resource.getTitle().isBlank() ? resource.getTitle() : "untitled";
        meta.setName(isBlank(overrideName) ? fallbackTitle : overrideName.trim());
        meta.setSize(resource.getFileSize());
        metaFileMapper.insertOrUpdate(meta);
        return meta;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
