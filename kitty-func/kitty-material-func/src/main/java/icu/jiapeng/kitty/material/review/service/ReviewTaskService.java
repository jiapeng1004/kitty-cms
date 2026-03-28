package icu.jiapeng.kitty.material.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.review.entity.KtReviewTask;

import java.util.List;
import java.util.Optional;

public interface ReviewTaskService extends IService<KtReviewTask> {

    void saveTask(KtReviewTask task);

    void updateTask(KtReviewTask task);

    Optional<KtReviewTask> findById(String id);

    List<KtReviewTask> listByBizDesc(String bizType, String bizId);

    boolean existsPending(String bizType, String bizId);
}
