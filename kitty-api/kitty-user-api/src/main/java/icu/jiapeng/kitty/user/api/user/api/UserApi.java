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
package icu.jiapeng.kitty.user.api.user.api;

import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.api.user.dto.UserLoginParam;
import icu.jiapeng.kitty.user.api.user.dto.UserQueryPageDTO;
import icu.jiapeng.kitty.user.api.user.dto.UserRegister;
import icu.jiapeng.kitty.user.api.user.dto.UserUpdateDTO;
import icu.jiapeng.kitty.user.api.user.vo.LoginResultVo;
import icu.jiapeng.kitty.user.api.user.vo.UserListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(name = "用户API")
@Validated
public interface UserApi {
    @Operation(summary = "用户注册")
    @PostMapping("/api/user/register")
    @ApiResponse(description = "用户id")
    String register(@Valid @RequestBody UserRegister userRegister);

    @Operation(summary = "用户登录")
    @PostMapping("/api/user/login")
    LoginResultVo login(@Valid @RequestBody UserLoginParam userLoginParam);

    @Operation(summary = "退出登录")
    @PostMapping("/api/user/logout")
    void logout();

    @Operation(summary = "验证码")
    @GetMapping("/api/user/captcha")
    void captcha(HttpServletRequest request, HttpServletResponse response);

    @Operation(summary = "分页查询用户（管理端）")
    @GetMapping("/api/user/query")
    PageRespVo<UserListVO> query(UserQueryPageDTO query);

    @Operation(summary = "根据ID获取用户详情（管理端）")
    @GetMapping("/api/user/{id}")
    UserListVO getDetail(@NotBlank String id);

    @Operation(summary = "更新用户（管理端）")
    @PutMapping("/api/user/{id}")
    boolean update(@NotBlank String id, @Valid UserUpdateDTO dto);
}