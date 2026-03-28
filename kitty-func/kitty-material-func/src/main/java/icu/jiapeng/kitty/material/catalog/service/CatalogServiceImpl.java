package icu.jiapeng.kitty.material.catalog.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.catalog.CatalogBeansConvert;
import icu.jiapeng.kitty.material.catalog.constants.CatalogType;
import icu.jiapeng.kitty.material.catalog.dto.CatalogCreateDTO;
import icu.jiapeng.kitty.material.catalog.entity.KtCatalog;
import icu.jiapeng.kitty.material.catalog.mapper.KtCatalogMapper;
import icu.jiapeng.kitty.material.catalog.vo.CatalogNodeVO;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import icu.jiapeng.kitty.material.user.UserContextGateway;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
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
        KtCatalog parentEntity = null;
        if (StrUtil.isNotBlank(node.getParentId()) && !parentId.equals("0")) {
            parentEntity = getById(parentId);
            if (Objects.isNull(parentEntity)) {
                throw new BizException(ResultStatus.CATALOG_PARENT_FOUND);
            }
        }
        String currentUserId = userContextGateway.currentUserId();
        String parentOrVirtualId = parentId;
        // 如果是目标是私人栏目根节点则映射到虚拟的个人根节点
        if (Objects.equals(parentId, CatalogType.PRIVATE.getCatalogId())) {
            parentOrVirtualId = CatalogType.PRIVATE.getCatalogId() + "_" + currentUserId;
        }
        // 确保父栏目下没有同名的栏目
        boolean exists = lambdaQuery()
                .eq(KtCatalog::getParentId, parentOrVirtualId)
                .eq(KtCatalog::getName, node.getName())
                .exists();
        if (exists) {
            throw new BizException(ResultStatus.CATALOG_NAME_EXISTS);
        }
        // 获取年纪最小的兄长栏目,以此构建treeCode
        KtCatalog brother = lambdaQuery()
                .eq(StrUtil.isNotBlank(parentOrVirtualId) && !parentOrVirtualId.equals("0"), KtCatalog::getParentId, parentOrVirtualId)
                .orderByAsc(KtCatalog::getTreeCode)
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
        // 但如果父栏目是私人栏目则此处使用一个魔法父code使用虚拟的个人栏目code
        KtCatalog mpEntity = CatalogBeansConvert.INSTANCE.node2Mp(node);
        // 如果是私人栏目树则设置owner
        if (Objects.equals(parentId, CatalogType.PRIVATE.getCatalogId()) ||
                Objects.nonNull(parentEntity) && Objects.equals(parentEntity.getOwnerUserId(), currentUserId)) {
            mpEntity.setOwnerUserId(currentUserId);
        }
        if (Objects.equals(parentId, CatalogType.PRIVATE.getCatalogId())) {
            parentCode = CatalogType.PRIVATE.getCatalogId() + "_" + currentUserId + "/";
        }
        String treeCode = parentCode.concat(Integer.toString(childIndex, 36)).concat("/");
        mpEntity.setTreeCode(treeCode);
        boolean ok = save(mpEntity);
        if (!ok) {
            throw new IllegalStateException("mybatis-plus save failed: " + node);
        }
        return mpEntity.getId();
    }

    @Override
    public List<CatalogNodeVO> catalogTreeWithRolePermission(String roleId) {
        return this.catalogTreeWithRolePermission(List.of(roleId), false);
    }

    @Override
    public List<CatalogNodeVO> catalogTreeWithUserPermission(String userId) {
        // 获取这个用户的角色
        List<String> roleIds = userContextGateway.userRoleIds(userId);
        // 获取角色权限树
        List<CatalogNodeVO> catalogNodeVOS = this.catalogTreeWithRolePermission(roleIds, true);
        // 如果其中包含个人栏目,则添加个人栏目子树
        // 添加个人栏目子树并继承个人栏目根节点的权限
        CatalogNodeVO privateRoot = catalogNodeVOS.stream()
                .filter(node -> node.getId().startsWith(CatalogType.PRIVATE.getCatalogId()))
                .findFirst()
                .orElse(null);
        if (Objects.nonNull(privateRoot)) {
            // 个人栏目子树
            List<CatalogNodeVO> privateCatalogChildTree = this.findPrivateCatalogChildTree(userId);
            // 展开privateCatalogChildTree的child为集合
            for (CatalogNodeVO catalogNodeVO : flatten(privateCatalogChildTree)) {
                catalogNodeVO.setPermissionCodes(privateRoot.getPermissionCodes());
            }
            privateRoot.setChildren(privateCatalogChildTree);
        }
        return catalogNodeVOS;
    }

    protected List<CatalogNodeVO> flatten(List<CatalogNodeVO> treeList) {
        ArrayList<CatalogNodeVO> result = new ArrayList<>();
        for (CatalogNodeVO catalogNodeVO : treeList) {
            result.add(catalogNodeVO);
            List<CatalogNodeVO> children = catalogNodeVO.getChildren();
            if (CollUtil.isNotEmpty(children)) {
                result.addAll(flatten(children));
            }
        }
        return result;
    }


    /**
     * 查栏目子树
     *
     * @param rootTreeCode 儿树根节点
     * @return 栏目子树
     */
    @SuppressWarnings("all")
    protected List<CatalogNodeVO> findCatalogChildTreeByTreeCode(String rootId, String rootTreeCode) {
        var ref = new Object() {
            public String rootId = null;
        };
        List<CatalogNodeVO> childs = lambdaQuery()
                .likeRight(KtCatalog::getTreeCode, rootTreeCode)
                .list().stream().map(CatalogBeansConvert.INSTANCE::mp2Node)
                .peek(new Consumer<CatalogNodeVO>() {
                    @Override
                    public void accept(CatalogNodeVO catalogNodeVO) {
                        if (Objects.equals(rootId, catalogNodeVO.getId())) {
                            ref.rootId = catalogNodeVO.getId();
                        }
                    }
                })
                .toList();
        List<?> child2 = childs;
        // 如果存在id为rootId的节点,则返回该节点的子节点
        List<?> build = TreeUtil.build((List<TreeNode<String>>) child2, StrUtil.firstNonBlank(ref.rootId, rootId));
        return (List<CatalogNodeVO>) Optional.ofNullable(build).orElseGet(ArrayList::new);
    }

    /**
     * 查个人栏目子树
     *
     * @param userId 用户ID
     * @return 个人栏目下的子树
     */
    protected List<CatalogNodeVO> findPrivateCatalogChildTree(String userId) {
        return findCatalogChildTreeByTreeCode("pri", "pri_".concat(userId));
    }

    @SuppressWarnings("all")
    @Override
    public List<CatalogNodeVO> catalogTreeWithRolePermission(List<String> roleIds, boolean onlyCouldView) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return List.of();
        }
        // 1.exists查看所有有查看权限的栏目
        // roleIds先检查下sql注入必须是全字母数字下划线-的结构
        if (!roleIds.stream().allMatch(roleId -> roleId.matches("[a-zA-Z0-9_-]+"))) {
            throw new IllegalArgumentException("roleIds must be alpha-numeric with underscores");
        }
        String treeViewCode = MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW;
        List<KtCatalog> viewCatalogs = lambdaQuery()
                .exists(
                        onlyCouldView,
                        "SELECT 1 FROM kt_catalog_permission WHERE catalog_id = kt_catalog.id AND permission_code = '" + treeViewCode + "' AND allow_flag=true AND role_id IN (" + roleIds.stream().map(s -> "'" + s + "'").collect(Collectors.joining(",")) + ")"
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
        // 3.hutool转成树结构
        List<TreeNode<String>> nodes = (List) list;
        List build = TreeUtil.build(nodes, "0");
        return (List<CatalogNodeVO>) Optional.ofNullable(build).orElseGet(ArrayList::new);
    }


    @Override
    public List<CatalogNodeVO> catalogTreeWithUserPermission() {
        String currentUserId = userContextGateway.currentUserId();
        return this.catalogTreeWithUserPermission(currentUserId);
    }


    @Override
    public void requireOnCatalog(String catalogId, String permissionCode) {
        if (catalogId == null || catalogId.isBlank()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (permissionCode == null || permissionCode.isBlank()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        List<String> roles = userContextGateway.currentRoleIds();
        if (!catalogPermissionService.hasPermission(catalogId, permissionCode, roles)) {
            log.warn("material catalog permission denied (rule): catalogId={} permissionCode={}", catalogId, permissionCode);
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        /* 与 StpInterface 对齐：全局无此权限码时不放行（双因子） */
        if (!userContextGateway.currentPermissionCodes().contains(permissionCode)) {
            log.warn("material catalog permission denied (global code): catalogId={} permissionCode={}", catalogId, permissionCode);
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
    }
}