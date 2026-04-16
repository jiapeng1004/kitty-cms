package icu.jiapeng.kitty.material.resource.api;

import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.material.resource.dto.MaterialMetaFileBindDTO;
import icu.jiapeng.kitty.material.resource.dto.MaterialResourceListPageQueryDTO;
import icu.jiapeng.kitty.material.resource.dto.MaterialResourceListQueryDTO;
import icu.jiapeng.kitty.material.resource.dto.MaterialResourceUpsertDTO;
import icu.jiapeng.kitty.material.resource.dto.MaterialFolderCreateDTO;
import icu.jiapeng.kitty.material.resource.dto.MaterialFolderUploadPlanDTO;
import icu.jiapeng.kitty.material.resource.dto.MaterialResourceFingerprintDTO;
import icu.jiapeng.kitty.material.resource.vo.MaterialMetaFileVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceDetailVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceFingerprintPrecheckVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Material 资源 API（MVC 契约）。
 */
@Tag(name = "Material-资源")
public interface MaterialResourceApi {

    @Operation(summary = "资源列表（无 keyword/semanticText 走 DB；有关键词走 ES 全文；有语义文本走 ES 向量）")
    @GetMapping("/api/material/resource/list")
    List<MaterialResourceVO> list(@ModelAttribute MaterialResourceListQueryDTO query);

    @Operation(summary = "资源分页列表（未传 keyword/semantic 时走数据库分页；传了则与 list 同检索逻辑后在内存切片，总条数受检索条数上限影响）")
    @GetMapping("/api/material/resource/page")
    PageRespVo<MaterialResourceVO> page(@ModelAttribute MaterialResourceListPageQueryDTO query);

    @Operation(summary = "资源详情（含编目最新快照）")
    @GetMapping("/api/material/resource/detail")
    MaterialResourceDetailVO detail(@RequestParam("resourceId") String resourceId);

    @Operation(summary = "预览资源文件（鉴权通过后 302 至源文件地址；列表 previewUrl 指向本接口）")
    @GetMapping("/api/material/resource/preview")
    ResponseEntity<Void> preview(@RequestParam("resourceId") String resourceId);

    @Operation(summary = "视频关键帧图（抽帧/转码产物就绪后 302；未就绪时 404）")
    @GetMapping("/api/material/resource/keyframe")
    ResponseEntity<Void> keyframe(@RequestParam("resourceId") String resourceId);

    @Operation(summary = "新增资源")
    @PostMapping("/api/material/resource")
    MaterialResourceVO create(@Valid @RequestBody MaterialResourceUpsertDTO req);

    @Operation(summary = "更新资源")
    @PutMapping("/api/material/resource")
    MaterialResourceVO update(@Valid @RequestBody MaterialResourceUpsertDTO req);

    @Operation(summary = "创建文件夹资源")
    @PostMapping("/api/material/resource/folder")
    MaterialResourceVO createFolder(@Valid @RequestBody MaterialFolderCreateDTO req);

    @Operation(summary = "重建栏目资源路径")
    @PostMapping("/api/material/resource/path/rebuild")
    void rebuildPath(@Valid @RequestBody MaterialResourceListQueryDTO query);

    @Operation(summary = "目录结构保真上传入口编排")
    @PostMapping("/api/material/resource/folder/plan-upload")
    List<MaterialResourceVO> planFolderUpload(@Valid @RequestBody MaterialFolderUploadPlanDTO req);

    @Operation(summary = "保存资源文件指纹（文件大小+分段CRC）")
    @PostMapping("/api/material/resource/fingerprint")
    MaterialResourceVO saveFingerprint(@Valid @RequestBody MaterialResourceFingerprintDTO req);

    @Operation(summary = "资源秒传/排重前置校验（基于指纹）")
    @PostMapping("/api/material/resource/fingerprint/precheck")
    MaterialResourceFingerprintPrecheckVO precheckFingerprint(@Valid @RequestBody MaterialResourceFingerprintDTO req);

    @Operation(summary = "按资源查询物理文件记录（无记录时 204）")
    @GetMapping("/api/material/resource/meta-file")
    ResponseEntity<MaterialMetaFileVO> getMetaFile(@RequestParam("resourceId") String resourceId);

    @Operation(summary = "绑定资源与存储记录、对象键并写入 meta_file")
    @PostMapping("/api/material/resource/meta-file/bind")
    MaterialMetaFileVO bindMetaFile(@Valid @RequestBody MaterialMetaFileBindDTO req);
}
