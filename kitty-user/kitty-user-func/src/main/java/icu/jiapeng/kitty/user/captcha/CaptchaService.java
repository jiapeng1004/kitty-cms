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
package icu.jiapeng.kitty.user.captcha;

import org.springframework.beans.factory.InitializingBean;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 验证码服务策略接口。实现类通过 {@link CaptchaServiceType} 枚举维护类型，注册时使用 e.g. {@code CaptchaServiceType.HUTOOL_REDIS.getType()}。
 */
public interface CaptchaService extends InitializingBean {

    /** 请求参数名：调用方可传此参数指定验证码服务类型 */
    String CAPTCHA_TYPE_PARAM_NAME = "captchaType";

    /** 配置表 key：默认验证码服务类型，与 {@link icu.jiapeng.kitty.user.cfg.TenantConfigEnum#CAPTCHA_SERVICE_TYPE} 一致；获取配置请优先使用 {@link icu.jiapeng.kitty.user.cfg.service.KtConfigService#getVal(icu.jiapeng.kitty.user.cfg.TenantConfigEnum)}。 */
    String CONFIG_KEY_CAPTCHA_SERVICE_TYPE = "user.captcha.service.type";

    /**
     * 实现类型标识，用于 Factory 注册与解析。内部服务使用枚举维护，如 {@code CaptchaServiceType.HUTOOL_REDIS.getType()}。
     */
    String getType();

    @Override
    default void afterPropertiesSet() {
        Factory.register(this);
    }

    /**
     * 生成验证码，实现类负责存储（如 Redis）并返回验证码与图片字节，由调用方写入 response 输出流。
     *
     * @return 验证码字符与图片字节
     */
    CaptchaGenerateResult generateCaptcha();

    /**
     * 校验用户输入的验证码是否通过。
     *
     * @param userInput 用户输入的验证码
     * @return 是否通过
     */
    boolean validate(String userInput);

    final class Factory {
        private static final ConcurrentMap<String, CaptchaService> MAP = new ConcurrentHashMap<>();

        public static void register(CaptchaService service) {
            register(service.getType(), service);
        }

        /**
         * 先到注册成功；同一 type 再次注册则抛异常。
         */
        public static void register(String type, CaptchaService service) {
            CaptchaService existing = MAP.putIfAbsent(type, service);
            if (existing != null) {
                throw new IllegalStateException("验证码服务类型已注册: " + type + ", 现有实现: " + existing.getClass().getName());
            }
        }

        /**
         * 不存在时返回 Optional.empty()，由调用方决定后续处理（如 orElseThrow）。
         */
        public static Optional<CaptchaService> resolve(String type) {
            return Optional.ofNullable(MAP.get(type));
        }
    }
}
