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
package icu.jiapeng.kitty.user.tenant.api;

import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.tenant.dto.TenantCreateDTO;
import icu.jiapeng.kitty.user.tenant.dto.TenantQueryPageDTO;
import icu.jiapeng.kitty.user.tenant.dto.TenantUpdateDTO;
import icu.jiapeng.kitty.user.tenant.vo.TenantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@Tag(name = "租户API")
public interface TenantApi {

    /** 开放接口：登录页租户下拉列表，返回 id、name，无需鉴权 */
    @Operation(summary = "租户列表（登录页选择用）")
    @GetMapping("/api/tenant/list")
    List<TenantVO> list();

    @Operation(summary = "创建租户")
    @PostMapping("/api/tenant")
    @ApiResponse(description = "租户id")
    String create(TenantCreateDTO dto);

    @Operation(summary = "分页查询租户")
    @GetMapping("/api/tenant/query")
    PageRespVo<TenantVO> query(TenantQueryPageDTO query);

    @Operation(summary = "根据ID获取租户")
    @GetMapping("/api/tenant/{id}")
    TenantVO getById(String id);

    @Operation(summary = "更新租户")
    @PutMapping("/api/tenant/{id}")
    boolean update(String id, TenantUpdateDTO dto);

    @Operation(summary = "删除租户")
    @DeleteMapping("/api/tenant/{id}")
    boolean delete(String id);
}