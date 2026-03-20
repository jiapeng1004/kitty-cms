package icu.jiapeng.kitty.clickqk.service;

import icu.jiapeng.kitty.clickqk.entity.AkSk;
import io.smallrye.mutiny.Uni;

/**
 *
 *
 * @author jiapeng
 * @since 2026/3/21
 */
public interface AkSkAuthService {
    Uni<String> findSkByAk(String ak);

    /**
     * 按主键 id 查询 AkSk（带 Redis 缓存）
     */
    Uni<AkSk> findById(String id);
}
