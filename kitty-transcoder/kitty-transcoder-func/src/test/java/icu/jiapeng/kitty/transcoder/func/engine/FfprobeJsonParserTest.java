package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.ProbeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FfprobeJsonParser 单元测试。
 */
class FfprobeJsonParserTest {

    private FfprobeJsonParser parser;

    @BeforeEach
    void setUp() {
        parser = new FfprobeJsonParser();
    }

    @Test
    void parseEmptyOrBlankReturnsEmptyResult() throws Exception {
        ProbeResult r = parser.parse("");
        assertNotNull(r);
        assertNull(r.getWidth());
        assertNull(r.getDurationMs());

        r = parser.parse(null);
        assertNotNull(r);
    }

    @Test
    void parseValidVideoStream() throws Exception {
        String json = """
            {
              "streams": [
                { "codec_type": "video", "width": 1920, "height": 1080, "r_frame_rate": "30/1" },
                { "codec_type": "audio" }
              ],
              "format": { "duration": "120.5", "bit_rate": "5000000" }
            }
            """;
        ProbeResult r = parser.parse(json);
        assertNotNull(r);
        assertEquals(1920, r.getWidth());
        assertEquals(1080, r.getHeight());
        assertEquals(30.0, r.getFrameRate(), 0.01);
        assertTrue(Boolean.TRUE.equals(r.getHasVideo()));
        assertTrue(Boolean.TRUE.equals(r.getHasAudio()));
        assertEquals(120500L, r.getDurationMs());
        assertEquals(5000000L, r.getBitrate());
    }

    @Test
    void parseFrameRateFraction() throws Exception {
        String json = """
            { "streams": [ { "codec_type": "video", "width": 640, "height": 480, "avg_frame_rate": "30000/1001" } ] }
            """;
        ProbeResult r = parser.parse(json);
        assertNotNull(r);
        assertEquals(640, r.getWidth());
        assertTrue(r.getFrameRate() > 29.9 && r.getFrameRate() < 30.0);
    }

    @Test
    void parseNoVideoNoAudio() throws Exception {
        String json = """
            { "streams": [], "format": { "duration": "10" } }
            """;
        ProbeResult r = parser.parse(json);
        assertNotNull(r);
        assertNull(r.getWidth());
        assertNull(r.getHasVideo());
        assertNull(r.getHasAudio());
        assertEquals(10000L, r.getDurationMs());
    }
}
