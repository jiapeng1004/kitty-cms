package icu.jiapeng.kitty.material.transcode.api;

import icu.jiapeng.kitty.material.transcode.dto.CatalogTranscodeBindCreateDTO;
import icu.jiapeng.kitty.material.transcode.dto.MaterialTranscodeStrategyUpsertDTO;
import icu.jiapeng.kitty.material.transcode.vo.CatalogTranscodeBindVO;
import icu.jiapeng.kitty.material.transcode.vo.MaterialTranscodeStrategyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Material-转码策略与栏目绑定")
public interface MaterialTranscodePolicyApi {

    @Operation(summary = "转码策略列表")
    @GetMapping("/api/material/transcode/strategy/list")
    List<MaterialTranscodeStrategyVO> listStrategies();

    @Operation(summary = "创建转码策略")
    @PostMapping("/api/material/transcode/strategy")
    MaterialTranscodeStrategyVO createStrategy(@Valid @RequestBody MaterialTranscodeStrategyUpsertDTO req);

    @Operation(summary = "更新转码策略")
    @PutMapping("/api/material/transcode/strategy")
    MaterialTranscodeStrategyVO updateStrategy(@Valid @RequestBody MaterialTranscodeStrategyUpsertDTO req);

    @Operation(summary = "删除转码策略")
    @DeleteMapping("/api/material/transcode/strategy")
    void deleteStrategy(@RequestParam("id") String id);

    @Operation(summary = "栏目绑定列表")
    @GetMapping("/api/material/transcode/bind/list")
    List<CatalogTranscodeBindVO> listBinds(@RequestParam("catalogId") String catalogId);

    @Operation(summary = "新增栏目绑定")
    @PostMapping("/api/material/transcode/bind")
    CatalogTranscodeBindVO createBind(@Valid @RequestBody CatalogTranscodeBindCreateDTO req);

    @Operation(summary = "删除栏目绑定")
    @DeleteMapping("/api/material/transcode/bind")
    void deleteBind(@RequestParam("id") String id);
}
