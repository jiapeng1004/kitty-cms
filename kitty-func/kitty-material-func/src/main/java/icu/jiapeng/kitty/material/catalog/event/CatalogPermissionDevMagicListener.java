package icu.jiapeng.kitty.material.catalog.event;

import icu.jiapeng.kitty.material.catalog.constants.CatalogPermission;
import icu.jiapeng.kitty.material.catalog.dto.CatalogPermissionCellDTO;
import icu.jiapeng.kitty.material.catalog.service.CatalogPermissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * DEV-MAGIC：监听栏目创建事件，为 admin-role 自动补齐全部栏目权限。
 *
 * <p>仅在启用 profile: {@code dev-magic} 时生效。</p>
 */
@Slf4j
@Component
@Profile("dev-magic")
public class CatalogPermissionDevMagicListener {
    private static final String ADMIN_ROLE_ID = "role-admin";

    @Resource
    private CatalogPermissionService catalogPermissionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCatalogCreated(CatalogCreatedEvent event) {
        String catalogId = event.catalogId();
        for (CatalogPermission permission : CatalogPermission.values()) {
            CatalogPermissionCellDTO dto = new CatalogPermissionCellDTO();
            dto.setRoleId(ADMIN_ROLE_ID);
            dto.setCatalogId(catalogId);
            dto.setPermissionCode(permission.getPermissionCode());
            catalogPermissionService.grant(dto);
        }
        log.info("dev magic granted all catalog permissions: roleId={} catalogId={}", ADMIN_ROLE_ID, catalogId);
    }
}
