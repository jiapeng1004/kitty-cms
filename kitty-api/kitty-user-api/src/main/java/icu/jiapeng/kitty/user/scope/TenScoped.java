package icu.jiapeng.kitty.user.scope;


import icu.jiapeng.kitty.user.db.KtTenSchemaInterceptor;

/**
 * 租户上下文
 *
 * @author jiapeng
 * @since 2026/2/11
 */
public class TenScoped {
    private static final ScopedValue<String> holder = ScopedValue.newInstance();

    /**
     * {@linkplain KtTenSchemaInterceptor}
     *
     * @param tenantId 租户id
     * @param task     任务
     */
    public static void run(String tenantId, Runnable task) {
        ScopedValue.where(holder, tenantId).run(task);
    }

    public static <T, X extends Exception> T call(String tenantId, ScopedValue.CallableOp<T, X> objectThrowableCallableOp) throws X {
        return ScopedValue.where(holder, tenantId).call(objectThrowableCallableOp);
    }


    public static String getTenantId() {
        try {
            return holder.get();
        } catch (Exception e) {
            return null;
        }
    }
}
