package icu.jiapeng.kitty.clickqk.constants;

/**
 * 从 Vert.x middleware 注入到 RoutingContext 的认证上下文 key。
 */
public final class Constant {

    private Constant() {
    }

    public static final String X_AK = "X-AK";

    public static final String X_SIGN = "X-SIGN";

    public static final String X_TS = "X-TS";

    public static final String DB = "kittyclick";

    /** Redis：按 ak 缓存 sk */
    public static final String REDIS_CACHE_AKSK_BY_AK = "kittyclick:cache:aksk:ak:";

    /** Redis：按 id 缓存 AkSk 文档 JSON */
    public static final String REDIS_CACHE_AKSK_BY_ID = "kittyclick:cache:aksk:id:";

    /** Redis：异步上报队列（LPUSH / RPOP） */
    public static final String REDIS_REPORT_QUEUE = "kittyclick:report:queue";
}

