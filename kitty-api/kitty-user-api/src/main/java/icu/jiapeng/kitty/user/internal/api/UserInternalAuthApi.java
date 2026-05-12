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
package icu.jiapeng.kitty.user.internal.api;

import icu.jiapeng.kitty.user.internal.dto.SaTokenDaoUpdateBody;
import icu.jiapeng.kitty.user.internal.dto.SaTokenDaoUpdatePayload;
import icu.jiapeng.kitty.user.internal.dto.SaTokenTimeoutPayload;
import icu.jiapeng.kitty.user.internal.dto.SaTokenValuePayload;
import icu.jiapeng.kitty.user.internal.dto.TokenIntrospectionPayload;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务 internal 认证与 Sa-Token DAO 桥接 API（OpenFeign 与 Controller / ApiProvider 共用契约）。
 * <p>微服务默认由 {@code UserInternalAuthController}（{@code @RestController}）实现；聚合单体可激活 profile
 * {@code kitty-user-embedded-api}，将同一段代码复制为 {@code UserInternalAuthApiProvider} 且仅把
 * {@code @RestController} 换成 {@code @Service}，作为本地 {@link UserInternalAuthApi} 供注入（不暴露 MVC 路由）。</p>
 * <p>路径以本接口上的映射注解为唯一可信来源（相对 Feign 客户端 {@code path = "/kitty-user"} 前缀）。</p>
 */
@FeignClient(name = "kitty-user", contextId = "userInternalAuth")
public interface UserInternalAuthApi {

    @GetMapping("/internal/v1/auth/token")
    TokenIntrospectionPayload tokenIntrospection(@RequestParam String token);

    @GetMapping("/internal/v1/auth/sa-token-dao")
    SaTokenValuePayload saTokenDaoGet(@RequestParam String key);

    @GetMapping("/internal/v1/auth/sa-token-dao/timeout")
    SaTokenTimeoutPayload saTokenDaoTimeout(@RequestParam String key);

    @PutMapping(value = "/internal/v1/auth/sa-token-dao", consumes = MediaType.APPLICATION_JSON_VALUE)
    SaTokenDaoUpdatePayload saTokenDaoUpdate(@RequestBody SaTokenDaoUpdateBody body);
}
