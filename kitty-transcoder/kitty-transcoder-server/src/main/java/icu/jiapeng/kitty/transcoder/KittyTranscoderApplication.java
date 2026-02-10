package icu.jiapeng.kitty.transcoder;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class KittyTranscoderApplication {

    public static ConfigurableApplicationContext app = null;

    static void main(String[] args) {
        app = SpringApplication.run(KittyTranscoderApplication.class, args);
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(() -> {
            System.out.println("KittyTranscoderApplication is shutting down...");
        }));
    }
}
