package icu.jiapeng.kitty.oauth2.server.springweb.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

public interface OAuth2ConsentApi {
    @Operation(summary = "展示同意页", description = "GET /oauth2/consent。")
    @GetMapping("/oauth2/consent")
    void consentPage(HttpServletRequest request, HttpServletResponse response)
            throws IOException;

    @Operation(summary = "提交同意结果", description = "POST /oauth2/consent，表单 action。")
    @PostMapping(value = "/oauth2/consent", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void consentSubmit(
            @RequestParam("action") String action,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException;
}
