package icu.jiapeng.kitty.user.oauth2.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.oauth2.api.KtOauth2ClientApi;
import icu.jiapeng.kitty.user.oauth2.dto.Oauth2ClientCreateDTO;
import icu.jiapeng.kitty.user.oauth2.dto.Oauth2ClientQueryPageDTO;
import icu.jiapeng.kitty.user.oauth2.dto.Oauth2ClientUpdateDTO;
import icu.jiapeng.kitty.user.oauth2.service.KtOauth2ClientService;
import icu.jiapeng.kitty.user.oauth2.vo.Oauth2ClientVO;
import icu.jiapeng.kitty.user.permission.constants.KtPermissionCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
public class KtOauth2ClientController implements KtOauth2ClientApi {

    private final KtOauth2ClientService ktOauth2ClientService;

    @Override
    @GetMapping("/api/oauth2-client/query")
    @SaCheckPermission(KtPermissionCode.OAUTH2_CLIENT_VIEW)
    public PageRespVo<Oauth2ClientVO> query(Oauth2ClientQueryPageDTO query) {
        return ktOauth2ClientService.query(query);
    }

    @Override
    @GetMapping("/api/oauth2-client/{id}")
    @SaCheckPermission(KtPermissionCode.OAUTH2_CLIENT_VIEW)
    public Oauth2ClientVO getById(@NotBlank @PathVariable String id) {
        return ktOauth2ClientService.getDetail(id);
    }

    @Override
    @PostMapping("/api/oauth2-client")
    @SaCheckPermission(KtPermissionCode.OAUTH2_CLIENT_CREATE)
    public Oauth2ClientVO create(@Valid @RequestBody Oauth2ClientCreateDTO dto) {
        return ktOauth2ClientService.create(dto);
    }

    @Override
    @PutMapping("/api/oauth2-client/{id}")
    @SaCheckPermission(KtPermissionCode.OAUTH2_CLIENT_UPDATE)
    public boolean update(@NotBlank @PathVariable String id, @Valid @RequestBody Oauth2ClientUpdateDTO dto) {
        return ktOauth2ClientService.update(id, dto);
    }

    @Override
    @DeleteMapping("/api/oauth2-client/{id}")
    @SaCheckPermission(KtPermissionCode.OAUTH2_CLIENT_DELETE)
    public boolean delete(@NotBlank @PathVariable String id) {
        return ktOauth2ClientService.delete(id);
    }
}

