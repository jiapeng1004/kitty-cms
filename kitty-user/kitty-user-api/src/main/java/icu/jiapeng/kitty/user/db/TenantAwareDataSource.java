package icu.jiapeng.kitty.user.db;

import java.sql.SQLException;

/**
 *
 *
 * @author jiapeng
 * @since 2026/2/11
 */
public interface TenantAwareDataSource {

    String getSchema();

    /**
     * 获取租户的schema
     *
     * @param tenantId 租户id
     * @return schema
     */
    String getTenSchema(String tenantId);

    /**
     * 创建schema
     *
     * @param schemaName schemaName
     */
    void createSchema(String schemaName);


    void migrateTables(String targetSchema, String... ignoreTables) throws SQLException;

    /**
     * 迁移表结构
     *
     * @param sourceSchema 源schema
     * @param targetSchema 目标schema
     */
    void migrateTables(String sourceSchema, String targetSchema, String... ignoreTables) throws SQLException;
}
