package icu.jiapeng.kitty.material.resource.support;

import cn.hutool.core.util.StrUtil;
import icu.jiapeng.kitty.common.core.util.PathUtil;
import icu.jiapeng.kitty.material.resource.constants.FileEngineTypeEnum;
import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;
import icu.jiapeng.kitty.material.resource.mapper.KtFileStorageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 根据存储配置拼接可浏览器访问的 HTTP(S) 基址（对象存储 path-style：endpoint/bucket/key；磁盘：endpoint/key）。
 */
@Component
@RequiredArgsConstructor
public class MaterialStoragePublicUrlBuilder {

    private final KtFileStorageMapper fileStorageMapper;

    public String buildByStorageId(String storageId, String objectKey) {
        if (!StringUtils.hasText(storageId) || !StringUtils.hasText(objectKey)) {
            return null;
        }
        KtFileStorage storage = fileStorageMapper.selectById(storageId);
        return build(storage, objectKey);
    }

    public String build(KtFileStorage storage, String objectKey) {
        if (storage == null || !StringUtils.hasText(objectKey)) {
            return null;
        }
        String base = StrUtil.firstNonBlank(storage.getExternalEndpoint(), storage.getInternalEndpoint());
        if (!StringUtils.hasText(base)) {
            return null;
        }
        String normalizedBase = base.trim().replaceAll("/+$", "");
        String key = objectKey.trim().replaceFirst("^/+", "");
        if (FileEngineTypeEnum.OBJECT_STORAGE.getType().equals(storage.getStorageType())) {
            String bucket = storage.getBucket();
            if (!StringUtils.hasText(bucket)) {
                return null;
            }
            return PathUtil.builderPath(normalizedBase, bucket.trim(), key);
        }
        if (FileEngineTypeEnum.DISK.getType().equals(storage.getStorageType())) {
            return PathUtil.builderPath(normalizedBase, key);
        }
        return null;
    }
}
