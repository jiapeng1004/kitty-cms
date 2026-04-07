package icu.jiapeng.kitty.plugin.s3.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * S3兼容层配置属性
 */
@ConfigurationProperties(prefix = "kitty.s3")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class S3Properties {

    /**
     * 是否启用S3兼容层
     */
    private boolean enabled = true;

    /**
     * 服务端口
     */
    private int port = 9000;

    /**
     * 访问密钥AK
     */
    private String accessKey = "minioadmin";

    /**
     * 秘密密钥SK
     */
    private String secretKey = "minioadmin";

    /**
     * 签名版本：V2/V4
     */
    private String signatureVersion = "V4";

    /**
     * 存储后端配置
     */
    private Storage storage = new Storage();

    /**
     * 分布式锁配置
     */
    private Lock lock = new Lock();

    /**
     * 元数据存储配置
     */
    private Metadata metadata = new Metadata();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Storage {
        /**
         * 存储类型：local/minio/oss/cos
         */
        private String type = "local";

        /**
         * 本地存储配置
         */
        private Local local = new Local();

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Local {
            /**
             * 本地存储根路径
             */
            private String basePath = "./s3-data";
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Lock {
        /**
         * 锁类型：local/redis
         */
        private String type = "local";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        /**
         * 元数据存储类型：memory/mysql/mongodb
         */
        private String type = "memory";
    }
}