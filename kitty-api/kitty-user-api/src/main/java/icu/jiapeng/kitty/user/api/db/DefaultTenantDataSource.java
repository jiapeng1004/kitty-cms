package icu.jiapeng.kitty.user.api.db;

import icu.jiapeng.kitty.user.api.scope.TenScoped;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;

@SuppressWarnings("all")
public class DefaultTenantDataSource implements TenantAwareDataSource, InitializingBean {

    @Autowired
    private DataSource dataSource;

    private String mainSchema;

    @Override
    public String getSchema() {
        // 获取主schema
        String tenantId = TenScoped.getTenantId();
        return getTenSchema(tenantId);
    }

    @Override
    public String getTenSchema(String tenantId) {
        if (StringUtils.hasText(tenantId)) {
            return mainSchema + "_" + tenantId;
        }
        return mainSchema;
    }

    /**
     * 当前写死仅支持mysql建表
     * @param schemaName schemaName
     */
    @Override
    public void createSchema(String schemaName) {
        String sql = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void migrateTables(String targetSchema, String... ignoreTables) throws SQLException {
        migrateTables(mainSchema, targetSchema, ignoreTables);
    }

    /**
     * 当前写死仅支持mysql表名
     * @param sourceSchema 源schema
     * @param targetSchema 目标schema
     * @param ignoreTables
     * @throws SQLException
     */
    @Override
    public void migrateTables(String sourceSchema, String targetSchema, String... ignoreTables) throws SQLException {
        HashSet<String> ignoreTable = new HashSet<>();
        if (ignoreTables != null) {
            for (String table : ignoreTables) {
                ignoreTable.add(table.toLowerCase());
            }
        }
        // 获取主schema
        try (Connection sourceConn = dataSource.getConnection()) {
            DatabaseMetaData metaData = sourceConn.getMetaData();
            ResultSet tables = metaData.getTables(sourceSchema, sourceSchema, "%", new String[]{"TABLE"});
            try (Statement stmt = sourceConn.createStatement();) {
                // 在目标数据库中执行建表语句
                sourceConn.setSchema(targetSchema);
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    // 查询建表语句
                    ResultSet createTableRs = stmt.executeQuery("SHOW CREATE TABLE " + tableName);
                    if (createTableRs.next()) {
                        String createTableSQL = createTableRs.getString("Create Table");
                        // 排除过滤掉的表
                        if (ignoreTable.contains(tableName.toLowerCase())) {
                            continue;
                        }
                        try (Statement stC = sourceConn.createStatement();) {
                            stC.execute(createTableSQL);
                        }
                    }
                }
            } finally {
                sourceConn.setSchema(sourceSchema);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            mainSchema = connection.getSchema();
            if (!StringUtils.hasText(mainSchema)) {
                throw new IllegalStateException("""
                    未获取到主schema,jdbc url必须存在主要schema,对于mysql连接必须指定databaseTerm=SCHEMA连接参数
                    """);
            }
        }
    }
}
