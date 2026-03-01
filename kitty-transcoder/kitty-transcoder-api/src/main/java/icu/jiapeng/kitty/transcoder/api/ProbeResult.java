package icu.jiapeng.kitty.transcoder.api;

import lombok.Data;

/**
 * 音视频探测结果：幅面、码率、时长等，供 probe 步骤输出、if 步骤条件判断。
 */
@Data
public class ProbeResult {

    private Integer width;
    private Integer height;
    private Long bitrate;
    private Long durationMs;
    private Double frameRate;
    private Integer videoCodec;
    private Integer audioCodec;
    private Boolean hasVideo;
    private Boolean hasAudio;
}
