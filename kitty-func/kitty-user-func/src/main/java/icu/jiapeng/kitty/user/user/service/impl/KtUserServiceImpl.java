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
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import icu.jiapeng.kitty.user.captcha.CaptchaService;
import icu.jiapeng.kitty.user.captcha.CaptchaServiceType;
import icu.jiapeng.kitty.user.cfg.TenantConfigEnum;
import icu.jiapeng.kitty.user.cfg.service.KtConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.user.dto.UserQueryPageDTO;
import icu.jiapeng.kitty.user.user.dto.UserUpdateDTO;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.user.constans.UserStatus;
import icu.jiapeng.kitty.user.user.dto.UserLoginParam;
import icu.jiapeng.kitty.user.user.dto.UserRegister;
import icu.jiapeng.kitty.user.user.entity.KtUser;
import icu.jiapeng.kitty.user.user.mapper.KtUserUserMapper;
import icu.jiapeng.kitty.user.user.service.KtUserService;
import icu.jiapeng.kitty.user.user.vo.LoginResultVo;
import icu.jiapeng.kitty.user.user.vo.UserListVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class KtUserServiceImpl extends ServiceImpl<KtUserUserMapper, KtUser> implements KtUserService {

    @Resource
    private KtConfigService ktConfigService;


    @Override
    public String register(UserRegister userRegister) {
        // 查询是否有这个用户
        KtUser existsUser = lambdaQuery().eq(KtUser::getNickName, userRegister.getNickName()).last("LIMIT 1").one();
        if (existsUser != null) {
            throw BizException.of(ResultStatus.USER_NICK_NAME_EXIST);
        }
        // 查询手机号是否存在了
        KtUser one = lambdaQuery().eq(KtUser::getPhone, userRegister.getPhone()).last("LIMIT 1").one();
        if (one != null) {
            throw BizException.of(ResultStatus.USER_PHONE_EXIST);
        }
        // 校验手机号格式是否正确
        if (StrUtil.isNotBlank(userRegister.getPhone()) && !userRegister.getPhone().matches("^1[3-9]\\d{9}$")) {
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
        KtUser one1 = lambdaQuery().eq(KtUser::getEmail, userRegister.getEmail()).last("LIMIT 1").one();
        if (one1 != null) {
            throw BizException.of(ResultStatus.USER_EMAIL_EXIST);
        }
        // ====开始创建用户====
        KtUser ktUser = new KtUser();
        ktUser.setNickName(userRegister.getNickName());
        String hashPwd = BCrypt.hashpw(userRegister.getPwd());
        ktUser.setPwd(hashPwd);
        ktUser.setPhone(userRegister.getPhone());
        ktUser.setEmail(userRegister.getEmail());
        ktUser.setRealName(userRegister.getRealName());
        ktUser.setStatus(UserStatus.NORMAL.getStatus());
        // 插入用户
        if (save(ktUser)) {
            return ktUser.getId();
        } else {
            throw BizException.of(ResultStatus.NORMAL_ERROR);
        }
    }

    @Override
    public LoginResultVo login(UserLoginParam userLoginParam) {
        String type = ktConfigService.getVal(TenantConfigEnum.CAPTCHA_SERVICE_TYPE);
        if (StrUtil.isBlank(type)) {
            type = CaptchaServiceType.HUTOOL.getType();
        }
        CaptchaService captchaService = CaptchaService.Factory.resolve(type.trim())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        if (!captchaService.validate(userLoginParam.getCaptcha())) {
            throw BizException.of(ResultStatus.CAPTCHA_ERROR);
        }
        // 获取这个用户
        KtUser user = lambdaQuery().eq(KtUser::getNickName, userLoginParam.getUsername()).last("LIMIT 1").one();
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
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public PageRespVo<UserListVO> query(UserQueryPageDTO query) {
        LambdaQueryWrapper<KtUser> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getSearchKey())) {
            String key = query.getSearchKey();
            wrapper.and(w -> w.like(KtUser::getNickName, key)
                    .or().like(KtUser::getPhone, key)
                    .or().like(KtUser::getEmail, key));
        }
        Page<KtUser> page = page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return PageRespVo.<UserListVO>builder()
                .page(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .orders(query.getOrders())
                .records(page.getRecords().stream().map(this::entityToUserListVO).toList())
                .build();
    }

    @Override
    public UserListVO getDetail(String id) {
        KtUser entity = getById(id);
        return entity == null ? null : entityToUserListVO(entity);
    }

    @Override
    public boolean update(String id, UserUpdateDTO dto) {
        KtUser one = getById(id);
        if (one == null) {
            return false;
        }
        if (dto.getRealName() != null) {
            one.setRealName(dto.getRealName());
        }
        if (dto.getNickName() != null) {
            one.setNickName(dto.getNickName());
        }
        if (dto.getPhone() != null) {
            one.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            one.setEmail(dto.getEmail());
        }
        if (dto.getStatus() != null) {
            one.setStatus(dto.getStatus());
        }
        return updateById(one);
    }

    private UserListVO entityToUserListVO(KtUser entity) {
        UserListVO vo = new UserListVO();
        BeanUtil.copyProperties(entity, vo, "pwd", "deleted");
        return vo;
    }
}
