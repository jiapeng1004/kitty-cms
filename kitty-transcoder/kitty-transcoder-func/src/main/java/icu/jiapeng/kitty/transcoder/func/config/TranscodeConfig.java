package icu.jiapeng.kitty.transcoder.func.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Configuration
@ConfigurationProperties(prefix = "transcoder")
public class TranscodeConfig {

    @Getter
    private String tempDir = System.getProperty("java.io.tmpdir") + "/transcode";
    /** 流水线工作目录，所有步骤输出根路径（$WORK_DIR）。未配置时用 tempDir。 */
    private String workDir;
    @Getter
    private Output output = new Output();

    /** 流水线工作目录；为 null 或空时用 {@link #getTempDir()}。 */
    public String getWorkDir() {
        return (workDir != null && !workDir.isBlank()) ? workDir : tempDir;
    }

    @Setter
    @Getter
    public static class Output {
        private String httpPrefix = "";
    }
}
