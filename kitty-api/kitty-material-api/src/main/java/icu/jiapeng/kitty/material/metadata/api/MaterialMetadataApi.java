package icu.jiapeng.kitty.material.metadata.api;

import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataFieldUpsertDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataHistoryQueryDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataLastQueryDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataSaveDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataTemplateBindFieldsDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataTemplateQueryDTO;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataTemplateUpsertDTO;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataFieldVO;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataFormFieldVO;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataInstanceEntryVO;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataSnapshotVO;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Material-编目")
@FeignClient(name = "kitty-mam", contextId = "materialMetadata")
public interface MaterialMetadataApi {

    @Operation(summary = "创建编目模板")
    @PostMapping("/api/material/metadata/template")
    MaterialMetadataTemplateVO createTemplate(@Valid @RequestBody MaterialMetadataTemplateUpsertDTO req);

    @Operation(summary = "更新编目模板")
    @PutMapping("/api/material/metadata/template")
    MaterialMetadataTemplateVO updateTemplate(@Valid @RequestBody MaterialMetadataTemplateUpsertDTO req);

    @Operation(summary = "删除编目模板")
    @DeleteMapping("/api/material/metadata/template")
    void deleteTemplate(@RequestParam String id);

    @Operation(summary = "模板列表")
    @GetMapping("/api/material/metadata/template/list")
    List<MaterialMetadataTemplateVO> listTemplates(@ModelAttribute MaterialMetadataTemplateQueryDTO query);

    @Operation(summary = "模板字段绑定全量替换")
    @PutMapping("/api/material/metadata/template/fields")
    void bindTemplateFields(@Valid @RequestBody MaterialMetadataTemplateBindFieldsDTO req);

    @Operation(summary = "查询模板已绑定字段（排序）")
    @GetMapping("/api/material/metadata/template/bindings")
    List<MaterialMetadataFormFieldVO> listTemplateBindings(@RequestParam String templateId);

    @Operation(summary = "创建编目字段定义")
    @PostMapping("/api/material/metadata/field")
    MaterialMetadataFieldVO createField(@Valid @RequestBody MaterialMetadataFieldUpsertDTO req);

    @Operation(summary = "更新编目字段定义")
    @PutMapping("/api/material/metadata/field")
    MaterialMetadataFieldVO updateField(@Valid @RequestBody MaterialMetadataFieldUpsertDTO req);

    @Operation(summary = "删除编目字段定义")
    @DeleteMapping("/api/material/metadata/field")
    void deleteField(@RequestParam String id);

    @Operation(summary = "字段定义列表")
    @GetMapping("/api/material/metadata/field/list")
    List<MaterialMetadataFieldVO> listFields();

    @Operation(summary = "按资源+模板拉取可填字段（已校验适用性）")
    @GetMapping("/api/material/metadata/instance/form-fields")
    List<MaterialMetadataFormFieldVO> formFieldsForResource(
            @RequestParam String resourceId,
            @RequestParam String templateId);

    @Operation(summary = "保存编目实例（新版本）")
    @PostMapping("/api/material/metadata/instance/save")
    MaterialMetadataSnapshotVO saveInstance(@Valid @RequestBody MaterialMetadataSaveDTO req);

    @Operation(summary = "查询最新编目（last=true）")
    @GetMapping("/api/material/metadata/instance/last")
    MaterialMetadataSnapshotVO getLast(@ModelAttribute MaterialMetadataLastQueryDTO query);

    @Operation(summary = "历史版本检索（version<=max）")
    @GetMapping("/api/material/metadata/instance/history")
    List<MaterialMetadataInstanceEntryVO> history(@ModelAttribute MaterialMetadataHistoryQueryDTO query);
}
