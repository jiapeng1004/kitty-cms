package icu.jiapeng.kitty.user.cfg.controller;


import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.cfg.convert.BeansConvert;
import icu.jiapeng.kitty.user.cfg.dto.ClassCreateDTO;
import icu.jiapeng.kitty.user.cfg.dto.ConfigClassPageDTO;
import icu.jiapeng.kitty.user.cfg.service.KtConfigClassService;
import icu.jiapeng.kitty.user.cfg.vo.ConfigClassListVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

/**
 *
 *
 * @author jiapeng
 * @since 2025/12/21
 */
@RestController
@RequestMapping("/api/configClass")
@Tag(name = "配置分类 API")
public class KtConfigClassController {
    @Resource
    private KtConfigClassService ktConfigClassService;

    @RequestMapping("/query")
    @Operation(description = "分页查询")
    public PageRespVo<ConfigClassListVo> query(@Valid ConfigClassPageDTO query) {
        return ktConfigClassService.query(query);
    }

    @PostMapping
    @Operation(description = "新增")
    public String create(ClassCreateDTO classCreateDTO) {
        return ktConfigClassService.create(classCreateDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(description = "删除")
    public Boolean remove(@NotBlank(message = "id.not.null") @PathVariable String id) {
        return ktConfigClassService.removeById(id);
    }

    @GetMapping("/{id}")
    @Operation(description = "查询")
    public ConfigClassListVo query(@NotBlank(message = "id.not.null") @PathVariable String id) {
        return BeansConvert.INSTANCE.class2ListVo(ktConfigClassService.getById(id));
    }
}
