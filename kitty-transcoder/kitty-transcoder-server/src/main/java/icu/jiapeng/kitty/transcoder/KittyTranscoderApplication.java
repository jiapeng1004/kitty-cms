package icu.jiapeng.kitty.transcoder;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KittyTranscoderApplication {

    void main(String[] args) {
        SpringApplication.run(KittyTranscoderApplication.class, args);
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(() -> {
            System.out.println("KittyTranscoderApplication is shutting down...");
        }));
    }
}
