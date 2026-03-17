package icu.jiapeng.kitty.user.db;


import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.statement.StatementHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 *
 *
 * @author jiapeng
 * @since 2026/2/12
 */
@RequiredArgsConstructor
public class KtTenSchemaInterceptor implements InnerInterceptor {

    private final TenantAwareDataSource tenantAwareDataSource;


    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        try {
            String schema = tenantAwareDataSource.getSchema();
            if (schema != null && !schema.isEmpty()) {
                String schemaCurrent = connection.getSchema();
                if (!Objects.equals(schemaCurrent, schema)) {
                    connection.setSchema(schema);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set schema: " + e.getMessage(), e);
        }
    }
}
