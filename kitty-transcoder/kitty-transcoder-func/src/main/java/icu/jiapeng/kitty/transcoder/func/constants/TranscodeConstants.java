package icu.jiapeng.kitty.transcoder.func.constants;

/**
 * 转码服务常量集中定义。同一语义的常量只在此处定义一处，多处引用，禁止在其它类中重复定义相同含义的常量值。
 */
public final class TranscodeConstants {

    private TranscodeConstants() {}

    /** Redis 键/频道前缀，全服务唯一 */
    public static final class RedisKeys {
        private RedisKeys() {}
        public static final String SESSION_KEY_PREFIX = "transcode:session:token:";
        public static final String NONCE_KEY_PREFIX = "transcode:signature:nonce:";
        public static final String TASK_QUEUE_KEY = "transcode:task:queue";
        public static final String TASK_LOCK_PREFIX = "transcode:task:lock:";
        public static final String CANCEL_KEY_PREFIX = "transcoder:cancel:";
        public static final String CANCEL_CHANNEL = "transcoder:cancel:channel";
    }

    /** 任务状态 */
    public static final class TaskStatus {
        private TaskStatus() {}
        public static final String PENDING = "PENDING";
        public static final String PROCESSING = "PROCESSING";
        public static final String COMPLETED = "COMPLETED";
        public static final String FAILED = "FAILED";
        public static final String CANCELLED = "CANCELLED";
    }

    /** 输入类型 */
    public static final class InputType {
        private InputType() {}
        public static final String DISK = "DISK";
        public static final String HTTP = "HTTP";
    }

    /** 任务类型 */
    public static final class TaskType {
        private TaskType() {}
        public static final String SCHEDULED_TRANSCODE = "SCHEDULED_TRANSCODE";
    }

    /** AccessKey 状态 */
    public static final class AccessKeyStatus {
        private AccessKeyStatus() {}
        public static final String ACTIVE = "ACTIVE";
    }

    /** 步骤失败异常消息前缀（用于解析 STEP_FAILED:stepId:message） */
    public static final String STEP_FAILED_PREFIX = "STEP_FAILED:";

    /** 请求属性：认证通过后写入的 accessKeyId */
    public static final String ATTR_ACCESS_KEY_ID = "transcode.accessKeyId";
}
