package icu.jiapeng.kitty.material.resource.fingerprint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

/**
 * 资源文件指纹：与 {@link icu.jiapeng.kitty.material.resource.service.MaterialResourceService} 约定一致。
 */
public final class ResourceFingerprintSupport {

    private ResourceFingerprintSupport() {
    }

    /**
     * {@code fileSize + ":" + crc32_0-crc32_1-...}（每段 CRC 为无符号 32 位十进制）。
     */
    public static String format(long fileSize, List<Long> chunkCrc32Unsigned) {
        String chunks = chunkCrc32Unsigned.stream().map(String::valueOf).collect(Collectors.joining("-"));
        return fileSize + ":" + chunks;
    }

    public static long crc32Unsigned(Path file) throws IOException {
        CRC32 crc = new CRC32();
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                crc.update(buf, 0, n);
            }
        }
        return crc.getValue() & 0xffffffffL;
    }
}
