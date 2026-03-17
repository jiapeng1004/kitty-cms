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
package icu.jiapeng.kitty.user.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@Import(KittyStpInterface.class)
public class SaConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 官方拦截器
        registry.addInterceptor(new SaInterceptor(auth -> {
                    StpUtil.checkLogin();
                })).addPathPatterns("/api/**")           // 拦截所有路径
                .excludePathPatterns("/api/user/login"   // 放行登录接口
                        , "/error" // 放行错误页面
                        , "/api/tenant/**" // 测试租户
                        , "/api/user/register" // 放行注册接口
                        , "/api/user/captcha" // 放行验证码接口
                        , "/basic/**" // 放行基础接口
                        , "/open/**" // 放行开放平台
                        , "/oauth2/**" // 放行OAuth2
                );
    }
}
