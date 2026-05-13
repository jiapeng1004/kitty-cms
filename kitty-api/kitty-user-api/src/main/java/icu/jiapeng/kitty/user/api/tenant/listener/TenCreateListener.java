package icu.jiapeng.kitty.user.api.tenant.listener;

import icu.jiapeng.kitty.user.api.tenant.vo.TenantVO;

/**
 *
 *
 * @author jiapeng
 * @since 2026/2/11
 */
public interface TenCreateListener {
    /**
     * 发布租户新增事件
     *
     * @param tenantVO 租户信息
     */
    void onTenCreate(TenantVO tenantVO);
}
