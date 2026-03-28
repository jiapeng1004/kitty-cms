package icu.jiapeng.kitty.material.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.task.entity.KtResourceTask;

import java.util.List;
import java.util.Optional;

public interface ResourceTaskService extends IService<KtResourceTask> {

    boolean save(KtResourceTask task);

    Optional<KtResourceTask> findById(String id);

    List<KtResourceTask> findVisibleByResourceId(String resourceId);
}
