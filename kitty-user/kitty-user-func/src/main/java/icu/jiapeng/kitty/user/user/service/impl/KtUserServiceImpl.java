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
package icu.jiapeng.kitty.user.user.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.user.constans.UserStatus;
import icu.jiapeng.kitty.user.user.dto.UserLoginParam;
import icu.jiapeng.kitty.user.user.dto.UserRegister;
import icu.jiapeng.kitty.user.user.entity.KtUserUser;
import icu.jiapeng.kitty.user.user.mapper.KtUserUserMapper;
import icu.jiapeng.kitty.user.user.service.KtUserService;
import icu.jiapeng.kitty.user.user.vo.LoginResultVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class KtUserServiceImpl extends ServiceImpl<KtUserUserMapper, KtUserUser> implements KtUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public String register(UserRegister userRegister) {
        // 查询是否有这个用户
        KtUserUser existsUser = lambdaQuery().eq(KtUserUser::getNickName, userRegister.getNickName()).last("LIMIT 1").one();
        if (existsUser != null) {
            throw BizException.of(ResultStatus.USER_NICK_NAME_EXIST);
        }
        // 查询手机号是否存在了
        KtUserUser one = lambdaQuery().eq(KtUserUser::getPhone, userRegister.getPhone()).last("LIMIT 1").one();
        if (one != null) {
            throw BizException.of(ResultStatus.USER_PHONE_EXIST);
        }
        // 校验手机号格式是否正确
        if (!userRegister.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw BizException.of(ResultStatus.USER_PHONE_FORMAT_ERROR);
        }
        // 校验密码是否复合规范,符号不低于8,大写,小写,数字 必须存在
        if (!userRegister.getPwd().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")) {
            throw BizException.of(ResultStatus.USER_PWD_FORMAT_ERROR);
        }
        if (StrUtil.isNotBlank(userRegister.getEmail())) {
            // 校验邮箱格式是否正确
            if (!userRegister.getEmail().matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
                throw BizException.of(ResultStatus.USER_EMAIL_FORMAT_ERROR);
            }
        }
        // 校验邮箱不存在
        KtUserUser one1 = lambdaQuery().eq(KtUserUser::getEmail, userRegister.getEmail()).last("LIMIT 1").one();
        if (one1 != null) {
            throw BizException.of(ResultStatus.USER_EMAIL_EXIST);
        }
        // ====开始创建用户====
        KtUserUser ktUserUser = new KtUserUser();
        ktUserUser.setNickName(userRegister.getNickName());
        String hashPwd = BCrypt.hashpw(userRegister.getPwd());
        ktUserUser.setPwd(hashPwd);
        ktUserUser.setPhone(userRegister.getPhone());
        ktUserUser.setEmail(userRegister.getEmail());
        ktUserUser.setRealName(userRegister.getRealName());
        ktUserUser.setStatus(UserStatus.NORMAL.getStatus());
        // 插入用户
        if (save(ktUserUser)) {
            return ktUserUser.getId();
        } else {
            throw BizException.of(ResultStatus.NORMAL_ERROR);
        }
    }

    @Override
    public LoginResultVo login(UserLoginParam userLoginParam) {
//        // 校验验证码
//        if (!"1".equals(stringRedisTemplate.opsForValue().get("user:login:captcha:" + userLoginParam.getCaptcha().toLowerCase()))) {
//            throw BizException.of(ResultStatus.CAPTCHA_ERROR);
//        } else {
//            stringRedisTemplate.delete("user:login:captcha:" + userLoginParam.getCaptcha());
//        }
        // 获取这个用户
        KtUserUser user = lambdaQuery().eq(KtUserUser::getNickName, userLoginParam.getUsername()).last("LIMIT 1").one();
        if (user == null) {
            throw BizException.of(ResultStatus.USER_PWD_NOT_EXIST);
        }
        // 比较密码
        if (!BCrypt.checkpw(userLoginParam.getPassword(), user.getPwd())) {
            throw BizException.of(ResultStatus.USER_PWD_NOT_EXIST);
        }
        // ==== 登录成功 ====
        // 颁发token
        StpUtil.login(user.getId());
        SaSession tokenSession = StpUtil.getTokenSession();
        long timeout = tokenSession.getTimeout();
        String token = tokenSession.getToken();
        LoginResultVo loginResultVo = new LoginResultVo();
        loginResultVo.setToken(token);
        loginResultVo.setTimeout(timeout);
        return loginResultVo;
    }

    @Override
    public void captcha(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("image/png");
        try {
            //hutool把验证码 生成图片
            LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 40, 4, 5);
            IoUtil.write(response.getOutputStream(), true, lineCaptcha.getImageBytes());
            stringRedisTemplate.opsForValue().set("user:login:captcha:" + lineCaptcha.getCode().toLowerCase(), "1", 61, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
