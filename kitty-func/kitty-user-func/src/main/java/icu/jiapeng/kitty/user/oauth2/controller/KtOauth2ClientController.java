package icu.jiapeng.kitty.user.oauth2.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.api.oauth2.api.KtOauth2ClientApi;
import icu.jiapeng.kitty.user.api.oauth2.dto.Oauth2ClientCreateDTO;
import icu.jiapeng.kitty.user.api.oauth2.dto.Oauth2ClientQueryPageDTO;
import icu.jiapeng.kitty.user.api.oauth2.dto.Oauth2ClientUpdateDTO;
import icu.jiapeng.kitty.user.oauth2.service.KtOauth2ClientService;
import icu.jiapeng.kitty.user.api.oauth2.vo.Oauth2ClientVO;
import icu.jiapeng.kitty.user.permission.constants.KtPermissionCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * OAuth2 客户端 API
 * <p>
 * 管理后台：OAuth2 注册客户端的增删改查。
 */
@Tag(name = "OAuth2 客户端 API", description = "管理后台：OAuth2 注册客户端 CRUD。")
@RestController
@Validated
@RequiredArgsConstructor
public class KtOauth2ClientController implements KtOauth2ClientApi {

    private final KtOauth2ClientService ktOauth2ClientService;

    /**
     * 分页查询 OAuth2 客户端
     */
    @Override
    @Operation(summary = "分页查询 OAuth2 客户端")
    @SaCheckPermission(KtPermissionCode.OAUTH2_CLIENT_VIEW)
    public PageRespVo<Oauth2ClientVO> query(Oauth2ClientQueryPageDTO query) {
        return ktOauth2ClientService.query(query);
    }

    /**
     * 根据ID获取 OAuth2 客户端
     */
    @Override
    @Operation(summary = "根据ID获取 OAuth2 客户端")
    @SaCheckPermission(KtPermissionCode.OAUTH2_CLIENT_VIEW)
    public Oauth2ClientVO getById(@NotBlank @PathVariable String id) {
        return ktOauth2ClientService.getDetail(id);
    }

    /**
     * 新增 OAuth2 客户端
     */
    @Override
    @Operation(summary = "新增 OAuth2 客户端")
    @SaCheckPermission(KtPermissionCode.OAUTH2_CLIENT_CREATE)
    public Oauth2ClientVO create(@Valid @RequestBody Oauth2ClientCreateDTO dto) {
        return ktOauth2ClientService.create(dto);
    }

    /**
     * 更新 OAuth2 客户端
     */
    @Override
    @Operation(summary = "更新 OAuth2 客户端")
    @SaCheckPermission(KtPermissionCode.OAUTH2_CLIENT_UPDATE)
    public boolean update(@NotBlank @PathVariable String id, @Valid @RequestBody Oauth2ClientUpdateDTO dto) {
        return ktOauth2ClientService.update(id, dto);
    }

    /**
     * 删除 OAuth2 客户端
     */
    @Override
    @Operation(summary = "删除 OAuth2 客户端")
    @SaCheckPermission(KtPermissionCode.OAUTH2_CLIENT_DELETE)
    public boolean delete(@NotBlank @PathVariable String id) {
        return ktOauth2ClientService.delete(id);
    }
}

