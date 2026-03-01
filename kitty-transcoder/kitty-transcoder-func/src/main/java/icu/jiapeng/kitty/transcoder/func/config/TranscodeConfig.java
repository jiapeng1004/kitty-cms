package icu.jiapeng.kitty.transcoder.func.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "transcoder")
public class TranscodeConfig {

    private String tempDir = System.getProperty("java.io.tmpdir") + "/transcode";
    private Output output = new Output();

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public static class Output {
        private String httpPrefix = "";

        public String getHttpPrefix() {
            return httpPrefix;
        }

        public void setHttpPrefix(String httpPrefix) {
            this.httpPrefix = httpPrefix;
        }
    }
}
