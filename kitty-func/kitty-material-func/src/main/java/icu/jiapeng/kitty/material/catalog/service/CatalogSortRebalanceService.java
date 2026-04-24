package icu.jiapeng.kitty.material.catalog.service;

import icu.jiapeng.kitty.material.catalog.event.CatalogParentSortTouchedEvent;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 事务提交后异步检查同父下 sort 是否逼近 int 极限（见 {@link CatalogSortRenormalizeExecutor}）。
 */
@Service
public class CatalogSortRebalanceService {

    /** 与 {@link CatalogServiceImpl} 中 SORT_STEP 一致，供归一化步长使用 */
    public static final int RENUMBER_STEP = 10;

    @Resource
    private CatalogSortRenormalizeExecutor catalogSortRenormalizeExecutor;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onParentSortTouched(CatalogParentSortTouchedEvent event) {
        catalogSortRenormalizeExecutor.renormalizeInParentIfOutOfRange(event.parentId());
    }
}
