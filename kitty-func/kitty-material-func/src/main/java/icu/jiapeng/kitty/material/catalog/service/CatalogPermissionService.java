package icu.jiapeng.kitty.material.catalog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.catalog.dto.CatalogPermissionCellDTO;
import icu.jiapeng.kitty.material.catalog.dto.CatalogPermissionUpsertDTO;
import icu.jiapeng.kitty.material.catalog.entity.KtCatalogPermission;

import java.util.List;
import java.util.Map;

/**
 * 栏目 + 权限码 + 角色 的判定领域契约：基于规则表解析允许/拒绝，并带短期结果缓存。
 * <p>
 * 规则语义：拒绝权限优先级高于允许权限，相同角色下优先拒绝。
 */
public interface CatalogPermissionService extends IService<KtCatalogPermission> {

    /**
     * 当前角色列表是否对某栏目拥有指定权限码；结果按 catalog+permission+角色集合 维度缓存数分钟。
     */
    boolean hasPermission(String catalogId, String permissionCode, List<String> roleIds);

    /**
     * 获取单角色/角色集合拥有栏目权限集合 Map<String, List<String>> 包含 栏目id-权限集合
     */
    Map<String, List<String>> getCatalogPermissions(List<String> roleIds);


    /**
     * 获取有指定权限的所有栏目ID列表
     * <p>
     * 注意：该方法直接在数据库层面进行查询，效率远高于逐个检查栏目的方式
     */
    List<String> getWithPermissionCatalogs(String permissionCode, List<String> roleIds);

    /**
     * 获取拥有的所有栏目权限码
     */
    List<String> getCatalogPermissionCodes(String catalogId, List<String> roleIds);


    /**
     * 更新权限（删除旧的 可修改权限，保留不可修改记录）
     */
    void upsert(List<KtCatalogPermission> newPermissions);

    /**
     * 全量替换：对 {@link CatalogPermissionUpsertDTO#getRoleIds()} 中每个角色，删除其可编辑规则后按 permissionItems 重写。
     */
    void upsert(CatalogPermissionUpsertDTO catalogPermissionUpsertDTO);

    /**
     * 单个授予（已存在同 uk 则忽略，仅针对可编辑语义下的新增）。
     */
    void grant(CatalogPermissionCellDTO dto);

    /**
     * 单个撤销（仅删除可编辑行）。
     */
    void revoke(CatalogPermissionCellDTO dto);
}
