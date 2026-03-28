package icu.jiapeng.kitty.material.resource.service;

import icu.jiapeng.kitty.material.resource.entity.KtMetaFile;

public interface MetaFileStorageBindService {

    KtMetaFile bind(String resourceId, String storageId, String objectKey, String overrideName);
}
