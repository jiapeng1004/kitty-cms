package icu.jiapeng.kitty.transcoder.func.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import icu.jiapeng.kitty.transcoder.api.ProbeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Iterator;

/**
 * 将 ffprobe -show_format -show_streams 的 JSON 输出解析为 ProbeResult。
 */
@Slf4j
@Component
public class FfprobeJsonParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ProbeResult parse(String json) throws Exception {
        if (json == null || json.isBlank()) {
            return new ProbeResult();
        }
        JsonNode root = MAPPER.readTree(json);
        ProbeResult result = new ProbeResult();
        JsonNode streams = root.get("streams");
        if (streams != null && streams.isArray()) {
            Integer width = null, height = null;
            Double frameRate = null;
            boolean hasVideo = false, hasAudio = false;
            for (Iterator<JsonNode> it = streams.elements(); it.hasNext(); ) {
                JsonNode s = it.next();
                String codecType = s.has("codec_type") ? s.get("codec_type").asText() : "";
                if ("video".equals(codecType)) {
                    hasVideo = true;
                    if (s.has("width")) width = s.get("width").asInt();
                    if (s.has("height")) height = s.get("height").asInt();
                    if (s.has("r_frame_rate")) {
                        String rf = s.get("r_frame_rate").asText();
                        frameRate = parseFrameRate(rf);
                    }
                    if (frameRate == null && s.has("avg_frame_rate")) {
                        String af = s.get("avg_frame_rate").asText();
                        frameRate = parseFrameRate(af);
                    }
                } else if ("audio".equals(codecType)) {
                    hasAudio = true;
                }
            }
            result.setWidth(width);
            result.setHeight(height);
            result.setFrameRate(frameRate);
            if (streams.size() > 0) {
                result.setHasVideo(hasVideo);
                result.setHasAudio(hasAudio);
            }
        }
        JsonNode format = root.get("format");
        if (format != null) {
            if (format.has("duration")) {
                try {
                    double sec = Double.parseDouble(format.get("duration").asText());
                    result.setDurationMs((long) (sec * 1000));
                } catch (NumberFormatException e) {
                    log.debug("parse duration: {}", e.getMessage());
                }
            }
            if (format.has("bit_rate")) {
                try {
                    result.setBitrate(Long.parseLong(format.get("bit_rate").asText()));
                } catch (NumberFormatException e) {
                    log.debug("parse bit_rate: {}", e.getMessage());
                }
            }
        }
        // 与原有 JavaCV 兼容：videoCodec/audioCodec 为 Integer，ffprobe 仅有 codec_name，此处留空
        result.setVideoCodec(null);
        result.setAudioCodec(null);
        return result;
    }

    private static Double parseFrameRate(String fraction) {
        if (fraction == null || fraction.isBlank()) return null;
        int i = fraction.indexOf('/');
        if (i <= 0) {
            try {
                return Double.parseDouble(fraction.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        try {
            double num = Double.parseDouble(fraction.substring(0, i).trim());
            double den = Double.parseDouble(fraction.substring(i + 1).trim());
            return den > 0 ? num / den : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
