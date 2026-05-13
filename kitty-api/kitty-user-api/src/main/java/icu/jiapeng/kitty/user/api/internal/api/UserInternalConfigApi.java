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
package icu.jiapeng.kitty.user.api.internal.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@FeignClient(name = "kitty-user", contextId = "userInternalConfig")
public interface UserInternalConfigApi {

    @GetMapping(value = "/api/internal/v1/config/value", produces = MediaType.TEXT_PLAIN_VALUE)
    String configValue(@RequestParam String configKey);

    /**
     * @param namespace 逻辑命名空间（如 {@code material}）
     * @param key       命名空间内键
     * @return 未配置或下游不可用时为空
     */
    default Optional<String> getString(String namespace, String key) {
        return Optional.ofNullable(configValue(namespace + "." + key));
    }
}
