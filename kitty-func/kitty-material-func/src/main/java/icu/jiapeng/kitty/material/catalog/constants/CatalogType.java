package icu.jiapeng.kitty.material.catalog.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

/**
 *
 *
 * @author jiapeng
 * @since 2026/3/28
 */
@Getter
@AllArgsConstructor
public enum CatalogType {

    /**
     * 个人栏目根节点
     */
    PRIVATE("-1", "个人栏目"),
    ;

    private final String catalogId;

    private final String name;

    public static String privateCatalogIdByUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        return PRIVATE.catalogId + "_" + userId.trim();
    }

    public static boolean isPrivateRootCatalogId(String catalogId) {
        return StringUtils.hasText(catalogId) && PRIVATE.catalogId.equals(catalogId.trim());
    }

    public static boolean isPrivateUserCatalogId(String catalogId) {
        return StringUtils.hasText(catalogId) && catalogId.trim().startsWith(PRIVATE.catalogId + "_");
    }

    public static String privateUserIdFromCatalogId(String catalogId) {
        if (!isPrivateUserCatalogId(catalogId)) {
            return null;
        }
        String normalized = catalogId.trim();
        return normalized.substring((PRIVATE.catalogId + "_").length());
    }

    public static String normalizePermissionCatalogId(String catalogId) {
        if (!StringUtils.hasText(catalogId)) {
            return catalogId;
        }
        String normalized = catalogId.trim();
        if (isPrivateUserCatalogId(normalized)) {
            return PRIVATE.catalogId;
        }
        return normalized;
    }
}
