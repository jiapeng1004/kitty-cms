package icu.jiapeng.kitty.material.support.user;

import cn.dev33.satoken.stp.StpUtil;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.user.UserContextGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

/**
 * sa-token 用户上下文实现（6.2：角色/权限列表由 StpUtil 委派至已注册的 {@link cn.dev33.satoken.stp.StpInterface}，通常为 kitty-user gRPC 插件实现）。
 */
@Component
public class SaTokenUserContextGateway implements UserContextGateway {

    @Override
    public String currentUserId() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        return String.valueOf(loginId);
    }

    @Override
    public List<String> currentRoleIds() {
        return Stream.concat(StpUtil.getRoleList().stream(), Stream.of("public")).distinct().toList();
    }

    @Override
    public List<String> currentPermissionCodes() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            return List.of();
        }
        return StpUtil.getPermissionList();
    }

    @Override
    public List<String> userRoleIds(String userId) {
        return StpUtil.getRoleList(userId);
    }
}
