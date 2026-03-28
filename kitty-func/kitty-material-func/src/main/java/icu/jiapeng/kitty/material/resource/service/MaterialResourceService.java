package icu.jiapeng.kitty.material.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.resource.dto.*;
import icu.jiapeng.kitty.material.resource.entity.KtResource;
import icu.jiapeng.kitty.material.resource.vo.*;

import java.util.List;
import java.util.Optional;

public interface MaterialResourceService extends IService<KtResource> {

    Optional<KtResource> findById(String id);

    List<KtResource> findAll();

    KtResource saveResource(KtResource entity);

    KtResource createFolder(String title, String catalogId, String parentId);

    void rebuildPaths(String catalogId);

    List<KtResource> planFolderUpload(String catalogId, String parentId, List<String> relativePaths);

    MaterialResourceDetailVO detail(String resourceId);

    List<MaterialResourceVO> list(MaterialResourceListQueryDTO query);

    MaterialResourceVO create(MaterialResourceUpsertDTO req);

    MaterialResourceVO update(MaterialResourceUpsertDTO req);

    MaterialResourceVO createFolder(MaterialFolderCreateDTO req);

    void rebuildPath(MaterialResourceListQueryDTO query);

    List<MaterialResourceVO> planFolderUpload(MaterialFolderUploadPlanDTO req);

    MaterialResourceVO saveFingerprint(MaterialResourceFingerprintDTO req);

    Optional<MaterialMetaFileVO> findMetaFileByResource(String resourceId);

    MaterialMetaFileVO bindMetaFile(MaterialMetaFileBindDTO req);

    MaterialResourceFingerprintPrecheckVO precheckFingerprint(MaterialResourceFingerprintDTO req);
}
