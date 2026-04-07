package icu.jiapeng.kitty.plugin.s3.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * S3兼容层本地测试启动类
 * 使用默认本地实现：内存锁+本地文件存储+内存元数据
 * 无需任何外部依赖，直接运行即可
 */
@SpringBootApplication
public class S3LocalTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(S3LocalTestApplication.class, args);
        System.out.println("""
                ==============================================
                Kitty S3 兼容层本地测试服务启动成功！
                ==============================================
                服务地址: http://localhost:9000
                AccessKey: minioadmin
                SecretKey: minioadmin
                存储类型: 本地文件系统（临时目录）
                元数据存储: 内存
                分布式锁: 本地内存锁
                ==============================================
                测试命令:
                # 上传文件
                curl -X PUT -T "test.txt" http://localhost:9000/my-bucket/test.txt
                
                # 下载文件
                curl http://localhost:9000/my-bucket/test.txt -o test.txt
                
                # 删除文件
                curl -X DELETE http://localhost:9000/my-bucket/test.txt
                ==============================================
                """);
    }
}
