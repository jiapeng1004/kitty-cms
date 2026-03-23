package icu.jiapeng.kitty.user.oauth2.api;

import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.oauth2.dto.Oauth2ClientCreateDTO;
import icu.jiapeng.kitty.user.oauth2.dto.Oauth2ClientQueryPageDTO;
import icu.jiapeng.kitty.user.oauth2.dto.Oauth2ClientUpdateDTO;
import icu.jiapeng.kitty.user.oauth2.vo.Oauth2ClientVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.*;

/**
 * OAuth2 客户端 API
 * <p>
 * 管理后台：OAuth2 注册客户端的增删改查。
 */
@Tag(name = "OAuth2 客户端 API", description = "管理后台：OAuth2 注册客户端 CRUD。")
public interface KtOauth2ClientApi {

    /**
     * 分页查询 OAuth2 客户端
     */
    @Operation(summary = "分页查询 OAuth2 客户端")
    @GetExchange("/api/oauth2-client/query")
    PageRespVo<Oauth2ClientVO> query(Oauth2ClientQueryPageDTO query);

    /**
     * 根据ID获取 OAuth2 客户端
     */
    @Operation(summary = "根据ID获取 OAuth2 客户端")
    @GetExchange("/api/oauth2-client/{id}")
    Oauth2ClientVO getById(@NotBlank String id);

    /**
     * 新增 OAuth2 客户端
     */
    @Operation(summary = "新增 OAuth2 客户端")
    @PostExchange("/api/oauth2-client")
    Oauth2ClientVO create(@Valid @RequestBody Oauth2ClientCreateDTO dto);

    /**
     * 更新 OAuth2 客户端
     */
    @Operation(summary = "更新 OAuth2 客户端")
    @PutExchange("/api/oauth2-client/{id}")
    boolean update(@NotBlank String id, @Valid @RequestBody Oauth2ClientUpdateDTO dto);

    /**
     * 删除 OAuth2 客户端
     */
    @Operation(summary = "删除 OAuth2 客户端")
    @DeleteExchange("/api/oauth2-client/{id}")
    boolean delete(@NotBlank String id);
}
