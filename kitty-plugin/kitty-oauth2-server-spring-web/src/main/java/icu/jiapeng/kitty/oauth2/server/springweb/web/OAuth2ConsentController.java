package icu.jiapeng.kitty.oauth2.server.springweb.web;

import icu.jiapeng.kitty.oauth2.server.springweb.api.OAuth2AuthorizationServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * OAuth2 同意页
 * <p>
 * 非 IETF OAuth2 核心路径；本模块约定 {@code /oauth2/consent}。
 */
@Tag(
        name = "OAuth2 同意页",
        description = "授权流程中的用户同意界面（本模块约定路径）。")
@RestController
@RequiredArgsConstructor
public class OAuth2ConsentController {

    private final OAuth2AuthorizationServerService oauth2AuthorizationServerService;

    /**
     * 展示同意页
     * <p>
     * GET，渲染同意界面或重定向。
     */
    @Operation(summary = "展示同意页", description = "GET /oauth2/consent。")
    @GetMapping("/oauth2/consent")
    public void consentPage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        oauth2AuthorizationServerService.processConsentGet(request, response);
    }

    /**
     * 提交同意结果
     * <p>
     * POST，{@code application/x-www-form-urlencoded}，参数 {@code action=approve|deny}。
     */
    @Operation(summary = "提交同意结果", description = "POST /oauth2/consent，表单 action。")
    @PostMapping(value = "/oauth2/consent", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void consentSubmit(
            @RequestParam("action") String action,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        boolean approved = "approve".equalsIgnoreCase(action);
        oauth2AuthorizationServerService.processConsentPost(approved, request, response);
    }
}
