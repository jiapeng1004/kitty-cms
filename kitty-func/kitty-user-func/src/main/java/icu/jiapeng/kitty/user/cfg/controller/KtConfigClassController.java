package icu.jiapeng.kitty.user.cfg.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.cfg.api.ConfigClassApi;
import icu.jiapeng.kitty.user.cfg.convert.BeansConvert;
import icu.jiapeng.kitty.user.cfg.dto.ClassCreateDTO;
import icu.jiapeng.kitty.user.cfg.dto.ClassUpdateDTO;
import icu.jiapeng.kitty.user.cfg.dto.ConfigClassPageDTO;
import icu.jiapeng.kitty.user.cfg.service.KtConfigClassService;
import icu.jiapeng.kitty.user.cfg.vo.ConfigClassListVo;
import icu.jiapeng.kitty.user.permission.constants.KtPermissionCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @SaCheckPermission(KtPermissionCode.CONFIG_VIEW)
    public PageRespVo<ConfigClassListVo> query(@Valid ConfigClassPageDTO query) {
        return ktConfigClassService.query(query);
    }

    @Override
    @GetMapping("/{id}")
    @Operation(description = "根据ID获取")
    @SaCheckPermission(KtPermissionCode.CONFIG_VIEW)
    public ConfigClassListVo getById(@PathVariable String id) {
        return BeansConvert.INSTANCE.class2ListVo(ktConfigClassService.getById(id));
    }

    @Override
    @PostMapping
    @Operation(description = "新增")
    @SaCheckPermission(KtPermissionCode.CONFIG_CREATE)
    public String create(@Valid @RequestBody ClassCreateDTO classCreateDTO) {
        return ktConfigClassService.create(classCreateDTO);
    }

    @Override
    @PutMapping("/{id}")
    @Operation(description = "更新")
    @SaCheckPermission(KtPermissionCode.CONFIG_UPDATE)
    public Boolean update(@NotBlank(message = "{id.not.null}") @PathVariable String id,
                          @Valid @RequestBody ClassUpdateDTO dto) {
        return ktConfigClassService.update(id, dto);
    }

    @Override
    @DeleteMapping("/{id}")
    @Operation(description = "删除")
    @SaCheckPermission(KtPermissionCode.CONFIG_DELETE)
    public Boolean remove(@PathVariable String id) {
        return ktConfigClassService.removeById(id);
    }
}
