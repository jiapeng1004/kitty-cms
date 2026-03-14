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
package icu.jiapeng.kitty.user.captcha.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import icu.jiapeng.kitty.user.captcha.CaptchaGenerateResult;
import icu.jiapeng.kitty.user.captcha.CaptchaService;
import icu.jiapeng.kitty.user.captcha.CaptchaServiceType;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Hutool 图形验证码 + Redis 缓存实现。
 */
@Service
public class HutoolRedisCaptchaServiceImpl implements CaptchaService {

    private static final String REDIS_KEY_PREFIX = "user:login:captcha:";
    private static final long REDIS_TTL_SECONDS = 61;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String getType() {
        return CaptchaServiceType.HUTOOL_REDIS.getType();
    }

    @Override
    public CaptchaGenerateResult generateCaptcha() {
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 40, 4, 5);
        String code = lineCaptcha.getCode().trim().toLowerCase();
        byte[] imageBytes = lineCaptcha.getImageBytes();
        stringRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX + code, "1", REDIS_TTL_SECONDS, TimeUnit.SECONDS);
        return new CaptchaGenerateResult(code, imageBytes);
    }

    @Override
    public boolean validate(String userInput) {
        if (userInput == null || userInput.isBlank()) {
            return false;
        }
        String key = REDIS_KEY_PREFIX + userInput.trim().toLowerCase();
        boolean ok = "1".equals(stringRedisTemplate.opsForValue().get(key));
        if (ok) {
            stringRedisTemplate.delete(key);
        }
        return ok;
    }
}
