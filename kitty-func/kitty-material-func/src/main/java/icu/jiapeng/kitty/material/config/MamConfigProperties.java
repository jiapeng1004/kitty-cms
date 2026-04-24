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
package icu.jiapeng.kitty.material.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "kitty.mam")
@Getter
@Setter
public class MamConfigProperties {
    /**
     * 免登名单
     * 默认允许访问认证相关端点和公共资源
     */
    private String[] whiteList = {
            "/login",
            "/logout",
            "/error",
            "/favicon.ico",
            "/actuator/**",
            "/oauth2/**",
            "/.well-known/**"
    };

    /**
     * 黑名单
     * 默认为空，可根据需要添加禁止访问的路径
     */
    private String[] blackList = {};

    /**
     * JWT 公钥登录、密码传输等
     */
    private String rsaPublicKey = "";

    /**
     * JWT 私钥登录、密码传输等
     */
    private String rsaPrivateKey = "";

    /**
     * 服务间调用基本认证
     * 服务名:token
     */
    private String serviceBasic;

    /**
     * 忽略的租户迁移表
     */
    private Set<String> tenMigrateIgnore;

    /**
     * kitty-data 等服务根 URL（如 http://localhost:8081），用于行为/事件上报；空则仅写 MAM 侧日志、不转发
     */
    private String dataEventBaseUrl = "";
}