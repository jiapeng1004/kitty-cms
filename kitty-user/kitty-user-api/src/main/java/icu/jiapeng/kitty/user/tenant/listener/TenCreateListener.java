package icu.jiapeng.kitty.user.tenant.listener;

import icu.jiapeng.kitty.user.tenant.vo.TenantVO;

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
