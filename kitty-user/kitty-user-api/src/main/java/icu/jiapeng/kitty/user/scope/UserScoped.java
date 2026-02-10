package icu.jiapeng.kitty.user.scope;


/**
 * 租户上下文
 *
 * @author jiapeng
 * @since 2026/2/11
 */
public class UserScoped {
    private static final ScopedValue<String> holder = ScopedValue.newInstance();

    /**
     *
     * @param userId 用户id
     * @param task   任务
     */
    public static void run(String userId, Runnable task) {
        ScopedValue.where(holder, userId).run(task);
    }

    public static <T, X extends Exception> T call(String UserId, ScopedValue.CallableOp<T, X> objectThrowableCallableOp) throws X {
        return ScopedValue.where(holder, UserId).call(objectThrowableCallableOp);
    }


    public static String getUserId() {
        try {
            return holder.get();
        } catch (Exception e) {
            return null;
        }
    }
}
