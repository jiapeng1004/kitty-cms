package icu.jiapeng.kitty.user.db;


import jakarta.annotation.Resource;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Objects;

/**
 *
 *
 * @author jiapeng
 * @since 2026/2/12
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class MybatisPlusTenSchemaInterceptor implements Interceptor {
    @Resource
    private TenantAwareDataSource tenantAwareDataSource;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Connection connection = (Connection) invocation.getArgs()[0];
        String schema = tenantAwareDataSource.getSchema();
        if (schema != null && !schema.isEmpty()) {
            String schemaCurrent = connection.getSchema();
            if (!Objects.equals(schemaCurrent, schema)) {
                connection.setSchema(schema);
            }
        }
        return invocation.proceed();
    }
}
