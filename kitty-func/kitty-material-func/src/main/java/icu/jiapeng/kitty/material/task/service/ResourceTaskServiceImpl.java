package icu.jiapeng.kitty.material.task.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.material.task.entity.KtResourceTask;
import icu.jiapeng.kitty.material.task.mapper.KtResourceTaskMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResourceTaskServiceImpl extends ServiceImpl<KtResourceTaskMapper, KtResourceTask> implements ResourceTaskService {

    @Override
    public boolean save(KtResourceTask task) {
        return saveOrUpdate(task);
    }

    @Override
    public Optional<KtResourceTask> findById(String id) {
        return Optional.ofNullable(getById(id));
    }

    @Override
    public List<KtResourceTask> findVisibleByResourceId(String resourceId) {
        return lambdaQuery()
                .eq(KtResourceTask::getResourceId, resourceId)
                .eq(KtResourceTask::getDeleted, 0)
                .orderByDesc(KtResourceTask::getCreateTime)
                .list();
    }
}
