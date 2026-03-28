package icu.jiapeng.kitty.material.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.resource.entity.KtMetaFile;

import java.util.List;
import java.util.Optional;

public interface MetaFileService extends IService<KtMetaFile> {

    Optional<KtMetaFile> findById(String id);

    List<KtMetaFile> findAll();

    Optional<KtMetaFile> findByResourceId(String resourceId);
}
