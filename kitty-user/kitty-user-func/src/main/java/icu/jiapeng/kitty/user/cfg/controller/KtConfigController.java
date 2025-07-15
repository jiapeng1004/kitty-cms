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


import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.cfg.convert.BeansConvert;
import icu.jiapeng.kitty.user.cfg.dto.ConfigQueryPageDTO;
import icu.jiapeng.kitty.user.cfg.dto.GetValDTO;
import icu.jiapeng.kitty.user.cfg.dto.SetValDTO;
import icu.jiapeng.kitty.user.cfg.service.KtConfigService;
import icu.jiapeng.kitty.user.cfg.vo.ConfigListVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 *
 *
 * @author jiapeng
 * @since 2025/12/20
 */
@Tag(name = "配置 API")
@RestController("/api/config")
@Validated
public class KtConfigController {
    @Resource
    private KtConfigService ktConfigService;

    @Operation(description = "分页查询")
    @GetMapping("/query")
    public PageRespVo<ConfigListVo> query(ConfigQueryPageDTO query) {
        return ktConfigService.query(query);
    }

    @Operation(description = "获取配置项")
    @GetMapping("/{id}")
    public ConfigListVo get(@NotBlank(message = "id.not.null") @PathVariable String id) {
        return BeansConvert.INSTANCE.config2ListVo(ktConfigService.getById(id));
    }

    @Operation(description = "删除配置项")
    @DeleteMapping("/{id}")
    public Boolean remove(@NotBlank(message = "id.not.null") @PathVariable String id) {
        return ktConfigService.removeById(id);
    }

    @Operation(description = "获取配置值")
    @GetMapping("/getVal")
    public String getVal(GetValDTO getValDTO) {
        return ktConfigService.getVal(getValDTO);
    }

    @Operation(description = "设置配置值")
    @PostMapping("/setVal")
    public String setVal(@Valid SetValDTO setValDTO) {
        return ktConfigService.setVal(setValDTO);
    }
}
