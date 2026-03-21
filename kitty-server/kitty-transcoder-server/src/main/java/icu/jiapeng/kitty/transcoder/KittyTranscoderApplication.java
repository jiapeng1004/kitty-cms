package icu.jiapeng.kitty.transcoder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("icu.jiapeng.kitty.transcoder.**.mapper")
public class KittyTranscoderApplication {

    public static void main(String[] args) {
        SpringApplication.run(KittyTranscoderApplication.class, args);
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(() -> {
            System.out.println("KittyTranscoderApplication is shutting down...");
        }));
    }
}