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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 验证码生成结果：验证码字符与图片字节，由 Controller 写入 response 输出流。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaGenerateResult {

    /** 验证码字符（已由实现类写入 Redis 等存储，供后续校验） */
    private String code;

    /** 验证码图片字节，可写入 {@link jakarta.servlet.ServletOutputStream} */
    private byte[] imageBytes;
}
