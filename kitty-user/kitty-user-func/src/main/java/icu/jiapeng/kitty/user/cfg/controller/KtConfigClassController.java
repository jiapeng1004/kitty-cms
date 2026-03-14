package icu.jiapeng.kitty.user.cfg.controller;

import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.cfg.api.ConfigClassApi;
import icu.jiapeng.kitty.user.cfg.convert.BeansConvert;
import icu.jiapeng.kitty.user.cfg.dto.ClassCreateDTO;
import icu.jiapeng.kitty.user.cfg.dto.ClassUpdateDTO;
import icu.jiapeng.kitty.user.cfg.dto.ConfigClassPageDTO;
import icu.jiapeng.kitty.user.cfg.service.KtConfigClassService;
import icu.jiapeng.kitty.user.cfg.vo.ConfigClassListVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 配置分类 API 实现，契约见 {@link ConfigClassApi}（kitty-user-api）
 *
 * @author jiapeng
 * @since 2025/12/21
 */
@RestController
@RequestMapping("/api/configClass")
@Tag(name = "配置分类 API")
@Validated
public class KtConfigClassController implements ConfigClassApi {
    @Resource
    private KtConfigClassService ktConfigClassService;

    @Override
    @GetMapping("/query")
    @Operation(description = "分页查询")
    public PageRespVo<ConfigClassListVo> query(@Valid ConfigClassPageDTO query) {
        return ktConfigClassService.query(query);
    }

    @Override
    @GetMapping("/{id}")
    @Operation(description = "根据ID获取")
    public ConfigClassListVo getById(@PathVariable String id) {
        return BeansConvert.INSTANCE.class2ListVo(ktConfigClassService.getById(id));
    }

    @Override
    @PostMapping
    @Operation(description = "新增")
    public String create(@Valid @RequestBody ClassCreateDTO classCreateDTO) {
        return ktConfigClassService.create(classCreateDTO);
    }

    @Override
    @PutMapping("/{id}")
    @Operation(description = "更新")
    public Boolean update(@NotBlank(message = "{id.not.null}") @PathVariable String id,
                          @Valid @RequestBody ClassUpdateDTO dto) {
        return ktConfigClassService.update(id, dto);
    }

    @Override
    @DeleteMapping("/{id}")
    @Operation(description = "删除")
    public Boolean remove(@PathVariable String id) {
        return ktConfigClassService.removeById(id);
    }
}
