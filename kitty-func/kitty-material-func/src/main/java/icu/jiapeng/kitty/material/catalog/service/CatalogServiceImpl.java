package icu.jiapeng.kitty.material.catalog.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.common.core.util.TreeUtil;
import icu.jiapeng.kitty.material.catalog.CatalogBeansConvert;
import icu.jiapeng.kitty.material.catalog.constants.CatalogPermission;
import icu.jiapeng.kitty.material.catalog.constants.CatalogType;
import icu.jiapeng.kitty.material.catalog.dto.CatalogCreateDTO;
import icu.jiapeng.kitty.material.catalog.dto.CatalogUpdateDTO;
import icu.jiapeng.kitty.material.catalog.entity.KtCatalog;
import icu.jiapeng.kitty.material.catalog.entity.KtCatalogPermission;
import icu.jiapeng.kitty.material.catalog.event.CatalogCreatedEvent;
import icu.jiapeng.kitty.material.catalog.event.CatalogDeletedEvent;
import icu.jiapeng.kitty.material.catalog.event.CatalogUpdatedEvent;
import icu.jiapeng.kitty.material.catalog.mapper.KtCatalogMapper;
import icu.jiapeng.kitty.material.catalog.vo.CatalogNodeVO;
import icu.jiapeng.kitty.material.user.UserContextGateway;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 栏目服务实现。
 */
@Service
@Slf4j
public class CatalogServiceImpl extends ServiceImpl<KtCatalogMapper, KtCatalog> implements CatalogService {

    @Resource
    private UserContextGateway userContextGateway;

    @Resource
    private CatalogPermissionService catalogPermissionService;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public List<CatalogNodeVO> findByParentId(String parentId) {
        return lambdaQuery()
                .eq(KtCatalog::getParentId, parentId)
                .orderByAsc(KtCatalog::getSortNum)
                .orderByAsc(KtCatalog::getName)
                .list()
                .stream()
                .map(CatalogBeansConvert.INSTANCE::mp2Node)
                .toList();
    }

    @Override
    public List<CatalogNodeVO> findAllPublic() {
        return lambdaQuery()
                .eq(KtCatalog::getOwnerUserId, null)
                .orderByAsc(KtCatalog::getSortNum)
                .orderByAsc(KtCatalog::getName)
                .list()
                .stream()
                .map(CatalogBeansConvert.INSTANCE::mp2Node)
                .toList();
    }

    @Override
    public List<CatalogNodeVO> findAllPrivateByOwner(String ownerUserId) {
        return lambdaQuery()
                .eq(KtCatalog::getOwnerUserId, ownerUserId)
                .orderByAsc(KtCatalog::getSortNum)
                .orderByAsc(KtCatalog::getName)
                .list()
                .stream()
                .map(CatalogBeansConvert.INSTANCE::mp2Node)
                .toList();
    }

    @Override
    public Optional<CatalogNodeVO> findById(String id) {
        KtCatalog mpEntity = getById(id);
        return Optional.ofNullable(mpEntity).map(CatalogBeansConvert.INSTANCE::mp2Node);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(CatalogCreateDTO node) {
        // 确保父节点存在
        String parentId = node.getParentId();
        if (Objects.equals(parentId, CatalogType.PRIVATE.getCatalogId())) {
            // 个人栏目仅作为个人素材库入口，不允许再创建子栏目。
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtCatalog parentEntity = null;
        if (StrUtil.isNotBlank(node.getParentId()) && !parentId.equals("0")) {
            parentEntity = getById(parentId);
            if (Objects.isNull(parentEntity)) {
                throw new BizException(ResultStatus.CATALOG_PARENT_FOUND);
            }
            if (StrUtil.isNotBlank(parentEntity.getOwnerUserId())) {
                // 历史私有子栏目数据兼容：禁止继续向私有栏目下创建子栏目。
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
        }
        // 确保父栏目下没有同名的栏目
        boolean exists = lambdaQuery()
                .eq(KtCatalog::getParentId, parentId)
                .eq(KtCatalog::getName, node.getName())
                .exists();
        if (exists) {
            throw new BizException(ResultStatus.CATALOG_NAME_EXISTS);
        }
        // 获取年纪最小的兄长栏目,以此构建treeCode
        KtCatalog brother = lambdaQuery()
                .eq(StrUtil.isNotBlank(parentId) && !parentId.equals("0"), KtCatalog::getParentId, parentId)
                .orderByDesc(KtCatalog::getTreeCode)
                .last("limit 1")
                .one();
        int childIndex = 1;
        if (Objects.nonNull(brother)) {
            String brotherTreeCode = brother.getTreeCode();
            String[] split = brotherTreeCode.split("/");
            // 默认36进制使用/隔开,切记最后一定要带上/否则会有检索问题
            int brotherIndex = Integer.parseInt(split[split.length - 1], 36);
            childIndex = brotherIndex + 1;
        }
        String parentCode = Optional.ofNullable(parentEntity).map(KtCatalog::getTreeCode).orElse("");
        KtCatalog mpEntity = CatalogBeansConvert.INSTANCE.node2Mp(node);
        String treeCode = parentCode.concat(Integer.toString(childIndex, 36)).concat("/");
        mpEntity.setTreeCode(treeCode);
        boolean ok = save(mpEntity);
        if (!ok) {
            throw new IllegalStateException("mybatis-plus save failed: " + node);
        }
        applicationEventPublisher.publishEvent(new CatalogCreatedEvent(mpEntity.getId()));
        return mpEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CatalogUpdateDTO dto) {
        String catalogId = Optional.ofNullable(dto.getId()).map(String::trim).orElse("");
        if (catalogId.isBlank()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtCatalog entity = getById(catalogId);
        if (entity == null) {
            throw new IllegalArgumentException("栏目不存在");
        }
        String oldParentId = entity.getParentId();
        String oldTreeCode = entity.getTreeCode();
        String newName = dto.getName() == null ? entity.getName() : dto.getName().trim();
        String targetParentId = dto.getParentId() == null ? oldParentId : dto.getParentId().trim();
        Integer targetSortNum = dto.getSortNum() == null ? entity.getSortNum() : dto.getSortNum();

        if (newName.isBlank() || targetParentId.isBlank()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }

        boolean parentChanged = !Objects.equals(oldParentId, targetParentId);
        if (parentChanged) {
            requireOnCatalog(catalogId, CatalogPermission.CATALOG_UPDATE);
            requireOnCatalog(targetParentId, CatalogPermission.CATALOG_CREATE);
        } else {
            requireOnCatalog(catalogId, CatalogPermission.CATALOG_UPDATE);
        }

        KtCatalog targetParent = null;
        if (!"0".equals(targetParentId)) {
            targetParent = getById(targetParentId);
            if (targetParent == null) {
                throw new BizException(ResultStatus.CATALOG_PARENT_FOUND);
            }
            if (StrUtil.isNotBlank(targetParent.getOwnerUserId())) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            if (parentChanged && targetParent.getTreeCode() != null && oldTreeCode != null
                    && targetParent.getTreeCode().startsWith(oldTreeCode)) {
                throw new IllegalStateException("不能移动到当前栏目子节点下");
            }
        }

        boolean siblingNameExists = lambdaQuery()
                .eq(KtCatalog::getParentId, targetParentId)
                .eq(KtCatalog::getName, newName)
                .ne(KtCatalog::getId, catalogId)
                .exists();
        if (siblingNameExists) {
            throw new BizException(ResultStatus.CATALOG_NAME_EXISTS);
        }

        boolean sameName = Objects.equals(newName, entity.getName());
        boolean sameSort = Objects.equals(targetSortNum, entity.getSortNum());
        if (!parentChanged && sameName && sameSort) {
            return;
        }

        if (parentChanged) {
            String newParentCode = Optional.ofNullable(targetParent).map(KtCatalog::getTreeCode).orElse("");
            KtCatalog brother = lambdaQuery()
                    .eq(KtCatalog::getParentId, targetParentId)
                    .ne(KtCatalog::getId, catalogId)
                    .orderByDesc(KtCatalog::getTreeCode)
                    .last("limit 1")
                    .one();
            int childIndex = 1;
            if (brother != null && StrUtil.isNotBlank(brother.getTreeCode())) {
                String[] split = brother.getTreeCode().split("/");
                childIndex = Integer.parseInt(split[split.length - 1], 36) + 1;
            }
            String newTreeCode = newParentCode.concat(Integer.toString(childIndex, 36)).concat("/");

            entity.setParentId(targetParentId);
            entity.setTreeCode(newTreeCode);
            entity.setName(newName);
            entity.setSortNum(targetSortNum);
            updateById(entity);

            if (StrUtil.isNotBlank(oldTreeCode) && StrUtil.isNotBlank(newTreeCode)) {
                final String oldPrefix = Objects.requireNonNull(oldTreeCode);
                List<KtCatalog> descendants = lambdaQuery()
                        .likeRight(KtCatalog::getTreeCode, oldPrefix)
                        .list();
                descendants.removeIf(item -> Objects.equals(item.getId(), catalogId));
                for (KtCatalog child : descendants) {
                    String currentCode = child.getTreeCode();
                    if (currentCode != null && currentCode.startsWith(oldPrefix)) {
                        child.setTreeCode(newTreeCode + currentCode.substring(oldPrefix.length()));
                    }
                }
                if (!descendants.isEmpty()) {
                    updateBatchById(descendants);
                }
            }
            applicationEventPublisher.publishEvent(new CatalogUpdatedEvent(catalogId));
            return;
        }

        entity.setName(newName);
        entity.setSortNum(targetSortNum);
        updateById(entity);
        applicationEventPublisher.publishEvent(new CatalogUpdatedEvent(catalogId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String catalogId) {
        String id = Optional.ofNullable(catalogId).map(String::trim).orElse("");
        if (id.isBlank()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtCatalog entity = getById(id);
        if (entity == null) {
            throw new IllegalArgumentException("栏目不存在");
        }
        requireOnCatalog(id, CatalogPermission.CATALOG_DELETE);
        boolean hasChild = lambdaQuery().eq(KtCatalog::getParentId, id).exists();
        if (hasChild) {
            throw new IllegalStateException("请先删除子栏目");
        }
        boolean deleted = removeById(id);
        if (!deleted) {
            throw new IllegalStateException("删除栏目失败");
        }
        catalogPermissionService.lambdaUpdate()
                .eq(KtCatalogPermission::getCatalogId, id)
                .remove();
        applicationEventPublisher.publishEvent(new CatalogDeletedEvent(id));
    }

    @Override
    public List<CatalogNodeVO> catalogTreeWithRolePermission(String roleId) {
        return this.catalogTreeWithRolePermission(List.of(roleId), false);
    }

    @Override
    public List<CatalogNodeVO> catalogTreeWithUserPermission(String userId) {
        // 获取这个用户的角色
        List<String> roleIds = userContextGateway.userRoleIds(userId);
        // 个人栏目不再挂载子栏目，直接返回角色权限树即可。
        return this.catalogTreeWithRolePermission(roleIds, true);
    }

    @SuppressWarnings("all")
    @Override
    public List<CatalogNodeVO> catalogTreeWithRolePermission(List<String> roleIds, boolean onlyCouldView) {
        if (Objects.isNull(roleIds)) {
            roleIds = List.of();
        }
        roleIds = new ArrayList<>(roleIds);
        roleIds.add("public");
        if (CollectionUtils.isEmpty(roleIds)) {
            return List.of();
        }
        // 1.exists查看所有有查看权限的栏目
        // roleIds先检查下sql注入必须是全字母数字下划线-的结构
        if (!roleIds.stream().allMatch(roleId -> roleId.matches("[a-zA-Z0-9_-]+"))) {
            throw new IllegalArgumentException("roleIds must be alpha-numeric with underscores");
        }
        String treeViewCode = CatalogPermission.TREE_VIEW.getPermissionCode();
        List<KtCatalog> viewCatalogs = lambdaQuery()
                .exists(
                        onlyCouldView,
                        String.format("""
                                SELECT 1 FROM kt_catalog_permission 
                                WHERE catalog_id = kt_catalog.id 
                                AND permission_code = '%s' 
                                AND allow_flag=true AND role_id IN (%s)
                                """, treeViewCode, roleIds.stream().map(s -> "'" + s + "'").collect(Collectors.joining(",")))
                )
                .list();
        // 2.获取权限map
        Map<String, List<String>> catalogPermissions = catalogPermissionService.getCatalogPermissions(roleIds);
        // 3.转为VO
        List<CatalogNodeVO> list = viewCatalogs.stream()
                .map(CatalogBeansConvert.INSTANCE::mp2Node)
                .peek(node -> {
                    List<String> permissionCodes = catalogPermissions.get(node.getId());
                    node.setPermissionCodes(permissionCodes);
                })
                .toList();
        // 3.构建树结构
        List<CatalogNodeVO> build = TreeUtil.build(list, "0");
        return Optional.ofNullable(build).orElseGet(ArrayList::new);
    }


    @Override
    public List<CatalogNodeVO> catalogTreeWithUserPermission() {
        String currentUserId = userContextGateway.currentUserId();
        return this.catalogTreeWithUserPermission(currentUserId);
    }


    @Override
    public void requireOnCatalog(String catalogId, CatalogPermission permission) {
        if (catalogId == null || catalogId.isBlank()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (permission == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String permissionCode = permission.getPermissionCode();
        String normalizedCatalogId = catalogId.trim();
        if (CatalogType.isPrivateUserCatalogId(normalizedCatalogId)) {
            String ownerUserId = CatalogType.privateUserIdFromCatalogId(normalizedCatalogId);
            String currentUserId = userContextGateway.currentUserId();
            if (!Objects.equals(ownerUserId, currentUserId)) {
                log.warn("material catalog permission denied (private owner mismatch): catalogId={} currentUserId={}", normalizedCatalogId, currentUserId);
                throw BizException.of(ResultStatus.MATERIAL_CATALOG_PERMISSION_DENIED);
            }
        }
        String permissionCatalogId = CatalogType.normalizePermissionCatalogId(normalizedCatalogId);

        List<String> roles = userContextGateway.currentRoleIds();
        if (!catalogPermissionService.hasPermission(permissionCatalogId, permissionCode, roles)) {
            log.warn("material catalog permission denied (rule): catalogId={} permissionCode={}", catalogId, permissionCode);
            throw BizException.of(ResultStatus.MATERIAL_CATALOG_PERMISSION_DENIED);
        }
    }

    @Override
    public String normalizeResourceCatalogId(String catalogId) {
        if (catalogId == null || catalogId.isBlank()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String normalized = catalogId.trim();
        if (CatalogType.isPrivateRootCatalogId(normalized)) {
            return CatalogType.privateCatalogIdByUser(userContextGateway.currentUserId());
        }
        return normalized;
    }
}