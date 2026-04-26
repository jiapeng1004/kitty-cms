package icu.jiapeng.kitty.material.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
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

    PageRespVo<MaterialResourceVO> page(MaterialResourceListPageQueryDTO query);

    /**
     * 回收站专用分页（deleted=1），与 {@link #page} 职责分离。
     */
    PageRespVo<MaterialResourceVO> pageRecycle(MaterialResourceListPageQueryDTO query);

    MaterialResourceVO create(MaterialResourceUpsertDTO req);

    MaterialResourceVO update(MaterialResourceUpsertDTO req);

    MaterialResourceVO createFolder(MaterialFolderCreateDTO req);

    void rebuildPath(MaterialResourceListQueryDTO query);

    List<MaterialResourceVO> planFolderUpload(MaterialFolderUploadPlanDTO req);

    MaterialResourceVO saveFingerprint(MaterialResourceFingerprintDTO req);

    Optional<MaterialMetaFileVO> findMetaFileByResource(String resourceId);

    MaterialMetaFileVO bindMetaFile(MaterialMetaFileBindDTO req);

    MaterialResourceFingerprintPrecheckVO precheckFingerprint(MaterialResourceFingerprintDTO req);

    /**
     * 预览重定向目标：对象存储上的源文件绝对 URL（供 preview 接口 302）。
     */
    String resolvePreviewRedirectUrl(String resourceId);

    /** 视频关键帧：未产出时 empty */
    Optional<String> resolveKeyframeRedirectUrl(String resourceId);

    /** 按分级解析可下载的直链（缺省码率时非 COVER/SPRITE 可降级为源码并标注 actualDestinationType） */
    MaterialDownloadUrlVO resolveDownloadUrl(String resourceId, String destinationType);

    void reportDownload(MaterialDownloadReportItemDTO body);

    void reportDownloadBatch(MaterialDownloadReportBatchDTO body);

    /**
     * 批量移入回收站（逻辑删）。
     */
    void recycleToBin(MaterialResourceIdsDTO body);

    /**
     * 从回收站彻底删除：写入坟场、删除物理文件、物理删主表及关联子表行。
     */
    void purgeFromRecycle(MaterialResourceIdsDTO body);
}
