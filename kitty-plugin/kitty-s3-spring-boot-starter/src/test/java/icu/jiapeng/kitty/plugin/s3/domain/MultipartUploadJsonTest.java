package icu.jiapeng.kitty.plugin.s3.domain;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 验证 Fastjson 持久化后分片表仍能按序号取回（与 Map 键类型有关）。
 */
class MultipartUploadJsonTest {

    @Test
    void roundTrip_parts_stringKeys() {
        MultipartUpload u = new MultipartUpload("uid", "b", "k", "application/octet-stream");
        u.addPart(1, "e1", 100);
        u.addPart(10, "e10", 200);
        u.addPart(2, "e2", 50);

        String json = JSON.toJSONString(u);
        MultipartUpload back = JSON.parseObject(json, MultipartUpload.class);

        assertNotNull(back.getParts());
        assertNotNull(back.getPart(1));
        assertNotNull(back.getPart(2));
        assertNotNull(back.getPart(10));
        assertEquals("e1", back.getPart(1).getETag());
        assertEquals("e10", back.getPart(10).getETag());
    }
}
