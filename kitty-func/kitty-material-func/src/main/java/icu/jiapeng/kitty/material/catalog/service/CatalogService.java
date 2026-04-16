package icu.jiapeng.kitty.material.catalog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.catalog.constants.CatalogPermission;
import icu.jiapeng.kitty.material.catalog.dto.CatalogCreateDTO;
import icu.jiapeng.kitty.material.catalog.dto.CatalogUpdateDTO;
import icu.jiapeng.kitty.material.catalog.entity.KtCatalog;
import icu.jiapeng.kitty.material.catalog.vo.CatalogNodeVO;

import java.util.List;
import java.util.Optional;

/**
 * 栏目服务接口。
 */
public interface CatalogService extends IService<KtCatalog> {

    /**
     * 按父级查询。
     */
    List<CatalogNodeVO> findByParentId(String parentId);


    /**
     * 查询公共栏目全集。
     */
    List<CatalogNodeVO> findAllPublic();

    /**
     * 查询用户私有栏目全集。
     */
    List<CatalogNodeVO> findAllPrivateByOwner(String ownerUserId);

    /**
     * 按 ID 查询。
     */
    Optional<CatalogNodeVO> findById(String id);

    /**
     * 保存/更新栏目节点。
     */
    String create(CatalogCreateDTO node);

    /**
     * 栏目更新（重命名/移动/排序）。
     */
    void update(CatalogUpdateDTO dto);

    /**
     * 删除栏目。
     */
    void delete(String catalogId);

    /**
     * 获取指定角色的权限栏目树
     * （包含不含栏目查看权限的）
     *
     * @param roleId 获取指定角色的权限栏目树
     * @return 角色权限栏目树
     */
    List<CatalogNodeVO> catalogTreeWithRolePermission(String roleId);

    /**
     * 获取指定用户的权限栏目树(含公共栏目和私有栏目)
     * （不包含无栏目查看权限的）
     */
    List<CatalogNodeVO> catalogTreeWithUserPermission(String userId);

    /**
     * 获取指定角色集合的权限栏目树(仅获取有查看权限的)
     * @param roleIds 角色id集合
     * @param onlyCouldView 是否仅输出拥有查看权限的栏目
     */
    List<CatalogNodeVO> catalogTreeWithRolePermission(List<String> roleIds, boolean onlyCouldView);

    /**
     * 获取当前用户的权限栏目树
     */
    List<CatalogNodeVO> catalogTreeWithUserPermission();

    /**
     * 断言指定的栏目权限
     *
     * @param catalogId      栏目ID
     * @param permissionCode 权限码
     */
    void requireOnCatalog(String catalogId, CatalogPermission permission);

    /**
     * 规范化素材资源落库使用的栏目 ID。
     * 个人栏目根（-1）会映射为 -1_{currentUserId}。
     */
    String normalizeResourceCatalogId(String catalogId);
}