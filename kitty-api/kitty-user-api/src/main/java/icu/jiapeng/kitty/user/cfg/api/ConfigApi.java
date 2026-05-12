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
package icu.jiapeng.kitty.user.cfg.api;

import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.cfg.dto.*;
import icu.jiapeng.kitty.user.cfg.vo.ConfigListVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * 配置 API
 *
 * @author jiapeng
 */
@Tag(name = "配置 API")
public interface ConfigApi {

    @Operation(summary = "分页查询")
    @GetMapping("/api/config/query")
    PageRespVo<ConfigListVo> query(ConfigQueryPageDTO query);

    @Operation(summary = "根据ID获取")
    @GetMapping("/api/config/{id}")
    ConfigListVo getById(@NotBlank(message = "{id.not.null}") String id);

    @Operation(summary = "新增配置项")
    @PostMapping("/api/config")
    String create(@Valid ConfigCreateDTO dto);

    @Operation(summary = "更新配置项")
    @PutMapping("/api/config/{id}")
    Boolean update(@NotBlank(message = "{id.not.null}") String id, @Valid ConfigUpdateDTO dto);

    @Operation(summary = "删除配置项")
    @DeleteMapping("/api/config/{id}")
    Boolean remove(@NotBlank(message = "{id.not.null}") String id);

    @Operation(description = "获取配置值")
    @GetMapping("/getVal")
    String getVal(GetValDTO getValDTO);

    @Operation(description = "设置配置值")
    @PostMapping("/setVal")
    String setVal(@Valid @RequestBody SetValDTO setValDTO);
}
