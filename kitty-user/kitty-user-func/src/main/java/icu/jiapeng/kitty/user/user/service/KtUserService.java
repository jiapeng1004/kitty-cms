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
package icu.jiapeng.kitty.user.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.user.dto.UserLoginParam;
import icu.jiapeng.kitty.user.user.dto.UserQueryPageDTO;
import icu.jiapeng.kitty.user.user.dto.UserRegister;
import icu.jiapeng.kitty.user.user.dto.UserUpdateDTO;
import icu.jiapeng.kitty.user.user.entity.KtUserUser;
import icu.jiapeng.kitty.user.user.vo.LoginResultVo;
import icu.jiapeng.kitty.user.user.vo.UserListVO;
import org.jspecify.annotations.NonNull;

public interface KtUserService extends IService<@NonNull KtUserUser> {

    /**
     * 用户组测
     *
     * @param userRegister 用户注册参数
     * @return 用户 id
     */
    String register(UserRegister userRegister);

    /**
     * 登录
     *
     * @param userLoginParam 用户登录参数
     * @return
     */
    LoginResultVo login(UserLoginParam userLoginParam);

    /**
     * 退出登录：使当前会话 token 失效
     */
    void logout();

    /**
     * 分页查询用户（管理端）
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageRespVo<UserListVO> query(UserQueryPageDTO query);

    /**
     * 根据ID获取用户详情（管理端，不含密码）
     *
     * @param id 用户id
     * @return 用户信息
     */
    UserListVO getDetail(String id);

    /**
     * 更新用户（管理端）
     *
     * @param id  用户id
     * @param dto 更新参数
     * @return 是否成功
     */
    boolean update(String id, UserUpdateDTO dto);
}
