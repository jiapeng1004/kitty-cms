package icu.jiapeng.kitty.oauth2.server.springweb.port;

import icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2ConsentViewModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 渲染 consent 交互页（默认内置 HTML/Vue；宿主可替换为自有模板或外站重定向）。
 */
public interface OAuth2ConsentUiPort {

    void renderConsentPage(HttpServletRequest request, HttpServletResponse response, OAuth2ConsentViewModel model)
            throws IOException;
}
