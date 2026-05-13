/*
 * Copyright [2025] [贾鹏]
 *
 * kitty-cms采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 贾鹏: jiapeng_aoa@163.com。
 * 5.不可二次分发开源参与同类竞品，如有想法可联系 贾鹏: jiapeng_aoa@163.com商议合作。
 */
package icu.jiapeng.kitty.user.cfg.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.api.cfg.api.ConfigApi;
import icu.jiapeng.kitty.user.api.cfg.dto.*;
import icu.jiapeng.kitty.user.cfg.convert.BeansConvert;
import icu.jiapeng.kitty.user.cfg.service.KtConfigService;
import icu.jiapeng.kitty.user.api.cfg.vo.ConfigListVo;
import icu.jiapeng.kitty.user.permission.constants.KtPermissionCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配置 API 实现，契约见 {@link ConfigApi}（kitty-user-api）
 *
 * @author jiapeng
 * @since 2025/12/20
 */
@Tag(name = "配置 API")
@RestController
@Validated
public class KtConfigController implements ConfigApi {
    @Resource
    private KtConfigService ktConfigService;

    @Override
    @Operation(description = "分页查询")
    @SaCheckPermission(KtPermissionCode.CONFIG_VIEW)
    public PageRespVo<ConfigListVo> query(ConfigQueryPageDTO query) {
        return ktConfigService.query(query);
    }

    @Override
    @Operation(description = "获取配置项")
    @SaCheckPermission(KtPermissionCode.CONFIG_VIEW)
    public ConfigListVo getById(@NotBlank(message = "{id.not.null}") @PathVariable String id) {
        return BeansConvert.INSTANCE.config2ListVo(ktConfigService.getById(id));
    }

    @Override
    @Operation(description = "新增配置项")
    @SaCheckPermission(KtPermissionCode.CONFIG_CREATE)
    public String create(@Valid @RequestBody ConfigCreateDTO dto) {
        return ktConfigService.create(dto);
    }

    @Override
    @Operation(description = "更新配置项")
    @SaCheckPermission(KtPermissionCode.CONFIG_UPDATE)
    public Boolean update(@NotBlank(message = "{id.not.null}") @PathVariable String id,
                          @Valid @RequestBody ConfigUpdateDTO dto) {
        return ktConfigService.update(id, dto);
    }

    @Override
    @Operation(description = "删除配置项")
    @SaCheckPermission(KtPermissionCode.CONFIG_DELETE)
    public Boolean remove(@NotBlank(message = "{id.not.null}") @PathVariable String id) {
        return ktConfigService.removeById(id);
    }

    @Operation(description = "获取配置值")
    @Override
    @SaCheckPermission(KtPermissionCode.CONFIG_VIEW)
    public String getVal(GetValDTO getValDTO) {
        return ktConfigService.getVal(getValDTO);
    }

    @Operation(description = "设置配置值")
    @Override
    @SaCheckPermission(KtPermissionCode.CONFIG_SET)
    public String setVal(@Valid @RequestBody SetValDTO setValDTO) {
        return ktConfigService.setVal(setValDTO);
    }
}
