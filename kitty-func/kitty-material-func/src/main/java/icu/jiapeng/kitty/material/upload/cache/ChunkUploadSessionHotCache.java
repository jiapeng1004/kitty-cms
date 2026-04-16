package icu.jiapeng.kitty.material.upload.cache;

import com.alibaba.fastjson.JSON;
import icu.jiapeng.kitty.material.upload.entity.KtChunkUploadSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 分片上传「热路径」状态：使用 {@link StringRedisTemplate}，避免多 Pod 高并发下对 DB 的逐片 insert/查询。
 * <p>
 * 会话元数据与分片登记以 Redis 为主；合并/完成前会刷入 DB（见 {@link icu.jiapeng.kitty.material.upload.service.ChunkUploadSessionService}）。
 * </p>
 * <p>
 * 分片 Hash 值：纯磁盘为 {@code byteSize}；对象存储为 {@code byteSize|partEtag}。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ChunkUploadSessionHotCache {

    static final String KEY_SESSION = "kitty:mam:chunk:session:";
    static final String KEY_PARTS = "kitty:mam:chunk:parts:";

    /** 上传中途会话在 Redis 中的存活时间（会话完成/取消后会主动删除 key） */
    private static final long SESSION_TTL_HOURS = 48L;

    private final StringRedisTemplate stringRedisTemplate;

    public void putSessionSnapshot(KtChunkUploadSession session) {
        if (session == null || !StringUtils.hasText(session.getId())) {
            return;
        }
        String json = JSON.toJSONString(session);
        String key = KEY_SESSION + session.getId();
        stringRedisTemplate.opsForValue().set(key, json, SESSION_TTL_HOURS, TimeUnit.HOURS);
        touchPartsTtl(session.getId());
    }

    /**
     * 优先读 Redis 快照；miss 时由调用方回源 DB 并回填。
     */
    public KtChunkUploadSession getSessionSnapshot(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return null;
        }
        String json = stringRedisTemplate.opsForValue().get(KEY_SESSION + sessionId);
        if (!StringUtils.hasText(json)) {
            return null;
        }
        return JSON.parseObject(json, KtChunkUploadSession.class);
    }

    /**
     * 原始 Hash 值（磁盘：仅 byteSize；对象存储：byteSize|etag）
     */
    public String getPartEntry(String sessionId, int chunkIndex) {
        if (!StringUtils.hasText(sessionId)) {
            return null;
        }
        Object v = stringRedisTemplate.opsForHash().get(KEY_PARTS + sessionId, String.valueOf(chunkIndex));
        return v == null ? null : v.toString();
    }

    public static long parseByteSizeFromPartEntry(String entry) {
        if (entry == null || entry.isBlank()) {
            return -1L;
        }
        int pipe = entry.indexOf('|');
        String n = pipe < 0 ? entry : entry.substring(0, pipe);
        return Long.parseLong(n.trim());
    }

    public static String parseEtagFromPartEntry(String entry) {
        if (entry == null) {
            return null;
        }
        int pipe = entry.indexOf('|');
        if (pipe < 0 || pipe >= entry.length() - 1) {
            return null;
        }
        return entry.substring(pipe + 1).trim();
    }

    /**
     * 登记本分片（幂等：同 index 同 byteSize 可重复写；对象存储需带 etag）。
     */
    public void recordPart(String sessionId, int chunkIndex, long byteSize, String partEtag) {
        String hk = KEY_PARTS + sessionId;
        String v = (partEtag == null || partEtag.isBlank())
                ? String.valueOf(byteSize)
                : byteSize + "|" + partEtag;
        stringRedisTemplate.opsForHash().put(hk, String.valueOf(chunkIndex), v);
        touchPartsTtl(sessionId);
        touchSessionTtl(sessionId);
    }

    public Map<String, String> loadPartEntries(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return Collections.emptyMap();
        }
        Map<Object, Object> raw = stringRedisTemplate.opsForHash().entries(KEY_PARTS + sessionId);
        if (raw.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> keys = new ArrayList<>(raw.size());
        for (Object k : raw.keySet()) {
            keys.add(k.toString());
        }
        Collections.sort(keys);
        java.util.LinkedHashMap<String, String> out = new java.util.LinkedHashMap<>();
        for (String k : keys) {
            out.put(k, raw.get(k).toString());
        }
        return out;
    }

    public void deleteUploadState(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        stringRedisTemplate.delete(KEY_SESSION + sessionId);
        stringRedisTemplate.delete(KEY_PARTS + sessionId);
    }

    private void touchPartsTtl(String sessionId) {
        stringRedisTemplate.expire(KEY_PARTS + sessionId, SESSION_TTL_HOURS, TimeUnit.HOURS);
    }

    private void touchSessionTtl(String sessionId) {
        stringRedisTemplate.expire(KEY_SESSION + sessionId, SESSION_TTL_HOURS, TimeUnit.HOURS);
    }
}
