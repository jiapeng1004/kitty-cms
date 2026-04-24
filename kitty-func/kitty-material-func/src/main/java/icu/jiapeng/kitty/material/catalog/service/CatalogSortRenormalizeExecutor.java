package icu.jiapeng.kitty.material.catalog.service;

import icu.jiapeng.kitty.material.catalog.entity.KtCatalog;
import icu.jiapeng.kitty.material.catalog.mapper.KtCatalogMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 同父下 sort 归一化（独立 Bean，保证事务代理生效）。
 */
@Service
@Slf4j
public class CatalogSortRenormalizeExecutor {

    private static final int HALF = Integer.MAX_VALUE / 2;

    @Resource
    private KtCatalogMapper ktCatalogMapper;

    @Resource
    private CatalogService catalogService;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void renormalizeInParentIfOutOfRange(String parentId) {
        Map<String, Object> minMax = ktCatalogMapper.selectMinMaxSortInParent(parentId);
        if (minMax == null || minMax.isEmpty()) {
            return;
        }
        long minV = toLong(minMax.get("minV"));
        long maxV = toLong(minMax.get("maxV"));
        if (maxV <= HALF && Math.abs(minV) <= HALF) {
            return;
        }
        List<KtCatalog> rows = ktCatalogMapper.listByParentIdOrderBySort(parentId);
        if (rows.isEmpty()) {
            return;
        }
        int s = CatalogSortRebalanceService.RENUMBER_STEP;
        for (KtCatalog row : rows) {
            row.setSortNum(s);
            s += CatalogSortRebalanceService.RENUMBER_STEP;
        }
        catalogService.updateBatchById(rows);
        log.info("栏目 sort 同父归一化完成: parentId={}, 条数={}", parentId, rows.size());
    }

    private static long toLong(Object o) {
        if (o == null) {
            return 0L;
        }
        if (o instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(Objects.toString(o));
    }
}
