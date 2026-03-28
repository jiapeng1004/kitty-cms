package icu.jiapeng.kitty.material.review;

/**
 * 审核任务状态（与 {@code review_task.status} 一致）。
 */
public final class ReviewTaskStatuses {

    private ReviewTaskStatuses() {
    }

    public static final String PENDING = "pending";
    public static final String APPROVED = "approved";
    public static final String REJECTED = "rejected";
}
