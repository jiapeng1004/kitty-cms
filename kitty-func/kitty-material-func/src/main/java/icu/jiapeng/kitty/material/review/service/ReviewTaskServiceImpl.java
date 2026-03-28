package icu.jiapeng.kitty.material.review.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.material.review.entity.KtReviewTask;
import icu.jiapeng.kitty.material.review.mapper.KtReviewTaskMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewTaskServiceImpl extends ServiceImpl<KtReviewTaskMapper, KtReviewTask> implements ReviewTaskService {

    @Override
    public void saveTask(KtReviewTask task) {
        saveOrUpdate(task);
    }

    @Override
    public void updateTask(KtReviewTask task) {
        updateById(task);
    }

    @Override
    public Optional<KtReviewTask> findById(String id) {
        return Optional.ofNullable(getById(id));
    }

    @Override
    public List<KtReviewTask> listByBizDesc(String bizType, String bizId) {
        return list().stream()
                .filter(t -> bizType.equals(t.getBizType()) && bizId.equals(t.getBizId()))
                .toList();
    }

    @Override
    public boolean existsPending(String bizType, String bizId) {
        return list().stream()
                .anyMatch(t -> bizType.equals(t.getBizType()) && bizId.equals(t.getBizId()) && "PENDING".equals(t.getStatus()));
    }
}
