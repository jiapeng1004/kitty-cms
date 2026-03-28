package icu.jiapeng.kitty.material.catalog.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.material.catalog.dto.CatalogPermissionCellDTO;
import icu.jiapeng.kitty.material.catalog.dto.CatalogPermissionUpsertDTO;
import icu.jiapeng.kitty.material.catalog.entity.KtCatalogPermission;
import icu.jiapeng.kitty.material.catalog.mapper.KtCatalogPermissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 栏目权限服务实现。
 */
@Service
public class CatalogPermissionServiceImpl extends ServiceImpl<KtCatalogPermissionMapper, KtCatalogPermission> implements CatalogPermissionService {

    private static void validateRoleId(String roleId) {
        if (!StringUtils.hasText(roleId) || !roleId.trim().matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException("roleId must be non-blank alpha-numeric with underscores");
        }
    }

    @Override
    public Map<String, List<String>> getCatalogPermissions(List<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyMap();
        }
        // 查询角色拥有的所有权限
        List<KtCatalogPermission> allPermissions = lambdaQuery()
                .in(KtCatalogPermission::getRoleId, roleIds)
                .eq(KtCatalogPermission::getAllowFlag, true)
                .list();
        return allPermissions.stream()
                .collect(Collectors.groupingBy(KtCatalogPermission::getCatalogId, Collectors.mapping(KtCatalogPermission::getPermissionCode, Collectors.toList())));
    }


    @Override
    public boolean hasPermission(String catalogId, String permissionCode, List<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds) || !StringUtils.hasText(permissionCode)) {
            return false;
        }
        return lambdaQuery()
                .select(KtCatalogPermission::getCatalogId)
                .eq(KtCatalogPermission::getPermissionCode, permissionCode)
                .eq(KtCatalogPermission::getAllowFlag, true)
                .in(KtCatalogPermission::getRoleId, roleIds)
                .exists();
    }


    @Override
    @SuppressWarnings("all")
    public List<String> getWithPermissionCatalogs(String permissionCode, List<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return List.of();
        }
        return lambdaQuery()
                .select(KtCatalogPermission::getCatalogId)
                .eq(KtCatalogPermission::getPermissionCode, permissionCode)
                .eq(KtCatalogPermission::getAllowFlag, true)
                .in(KtCatalogPermission::getRoleId, roleIds)
                .list().stream().map(KtCatalogPermission::getCatalogId).distinct().toList();
    }

    @Override
    @SuppressWarnings("all")
    public List<String> getCatalogPermissionCodes(String catalogId, List<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return List.of();
        }
        return lambdaQuery()
                .select(KtCatalogPermission::getPermissionCode)
                .eq(KtCatalogPermission::getCatalogId, catalogId)
                .eq(KtCatalogPermission::getAllowFlag, true)
                .in(KtCatalogPermission::getRoleId, roleIds)
                .list().stream().map(KtCatalogPermission::getPermissionCode).distinct().toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsert(List<KtCatalogPermission> newPermissions) {
        if (newPermissions == null || newPermissions.isEmpty()) {
            return;
        }
        Set<String> roleIds = newPermissions.stream()
                .map(KtCatalogPermission::getRoleId)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            throw new IllegalArgumentException("newPermissions must set roleId on each row");
        }
        for (String roleId : roleIds) {
            validateRoleId(roleId);
        }
        lambdaUpdate()
                .in(KtCatalogPermission::getRoleId, roleIds)
                .ne(KtCatalogPermission::getEditable, false)
                .remove();
        saveBatch(newPermissions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsert(CatalogPermissionUpsertDTO dto) {
        List<String> roleIds = dto.getRoleIds();
        if (CollectionUtils.isEmpty(roleIds)) {
            throw new IllegalArgumentException("roleIds must not be empty");
        }
        for (String roleId : roleIds) {
            validateRoleId(roleId);
        }
        Set<String> roleIdSet = new HashSet<>(roleIds);
        List<CatalogPermissionUpsertDTO.CatalogPermissionUpsertItemDTO> items =
                dto.getPermissionItems() == null ? List.of() : dto.getPermissionItems();
        for (CatalogPermissionUpsertDTO.CatalogPermissionUpsertItemDTO item : items) {
            if (!roleIdSet.contains(item.getRoleId())) {
                throw new IllegalArgumentException("permissionItems.roleId must be contained in roleIds");
            }
            validateRoleId(item.getRoleId());
        }

        List<String> codeScope = dto.getPermissionCodesReplace();
        if (CollectionUtils.isEmpty(codeScope)) {
            lambdaUpdate()
                    .in(KtCatalogPermission::getRoleId, roleIds)
                    .ne(KtCatalogPermission::getEditable, false)
                    .remove();
        } else {
            List<String> codes = codeScope.stream().filter(StringUtils::hasText).map(String::trim).distinct().toList();
            if (codes.isEmpty()) {
                throw new IllegalArgumentException("permissionCodesReplace must contain at least one non-blank permission code when provided");
            }
            lambdaUpdate()
                    .in(KtCatalogPermission::getRoleId, roleIds)
                    .in(KtCatalogPermission::getPermissionCode, codes)
                    .ne(KtCatalogPermission::getEditable, false)
                    .remove();
        }

        List<KtCatalogPermission> rows = new ArrayList<>();
        Set<String> dedupeKeys = new HashSet<>();
        for (CatalogPermissionUpsertDTO.CatalogPermissionUpsertItemDTO item : items) {
            List<String> codes = item.getPermissionCodes();
            if (CollectionUtils.isEmpty(codes)) {
                continue;
            }
            if (!StringUtils.hasText(item.getCatalogId())) {
                continue;
            }
            String catalogId = item.getCatalogId().trim();
            String rId = item.getRoleId().trim();
            for (String code : codes) {
                if (!StringUtils.hasText(code)) {
                    continue;
                }
                String pc = code.trim();
                String uk = rId + '\0' + catalogId + '\0' + pc;
                if (!dedupeKeys.add(uk)) {
                    continue;
                }
                KtCatalogPermission row = new KtCatalogPermission();
                row.setId(UUID.randomUUID().toString());
                row.setRoleId(rId);
                row.setCatalogId(catalogId);
                row.setPermissionCode(pc);
                row.setAllowFlag(true);
                row.setEditable(true);
                rows.add(row);
            }
        }
        if (!rows.isEmpty()) {
            saveBatch(rows);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grant(CatalogPermissionCellDTO dto) {
        String roleId = dto.getRoleId().trim();
        String catalogId = dto.getCatalogId().trim();
        String permissionCode = dto.getPermissionCode().trim();
        validateRoleId(roleId);
        if (!StringUtils.hasText(catalogId) || !StringUtils.hasText(permissionCode)) {
            throw new IllegalArgumentException("catalogId and permissionCode must not be blank");
        }
        boolean exists = lambdaQuery()
                .eq(KtCatalogPermission::getRoleId, roleId)
                .eq(KtCatalogPermission::getCatalogId, catalogId)
                .eq(KtCatalogPermission::getPermissionCode, permissionCode)
                .exists();
        if (exists) {
            return;
        }
        KtCatalogPermission row = new KtCatalogPermission();
        row.setId(UUID.randomUUID().toString());
        row.setRoleId(roleId);
        row.setCatalogId(catalogId);
        row.setPermissionCode(permissionCode);
        row.setAllowFlag(true);
        row.setEditable(true);
        save(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(CatalogPermissionCellDTO dto) {
        String roleId = dto.getRoleId().trim();
        String catalogId = dto.getCatalogId().trim();
        String permissionCode = dto.getPermissionCode().trim();
        validateRoleId(roleId);
        if (!StringUtils.hasText(catalogId) || !StringUtils.hasText(permissionCode)) {
            throw new IllegalArgumentException("catalogId and permissionCode must not be blank");
        }
        lambdaUpdate()
                .eq(KtCatalogPermission::getRoleId, roleId)
                .eq(KtCatalogPermission::getCatalogId, catalogId)
                .eq(KtCatalogPermission::getPermissionCode, permissionCode)
                .ne(KtCatalogPermission::getEditable, false)
                .remove();
    }
}
