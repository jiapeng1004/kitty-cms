package icu.jiapeng.kitty.material.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.resource.entity.KtResourceDerivative;

import java.util.List;
import java.util.Optional;

public interface ResourceDerivativeService extends IService<KtResourceDerivative> {

    List<KtResourceDerivative> listByResourceId(String resourceId);

    /**
     * 同一分级下，用于列表批量带封面等（如 COVER）；resourceIds 已去重且非空时查询。
     */
    List<KtResourceDerivative> listByResourceIdsAndDestinationType(Iterable<String> resourceIds, String destinationType);

    Optional<KtResourceDerivative> findByResourceAndType(String resourceId, String destinationType);

    void upsertExternal(String resourceId, String destinationType, String externalUrl, Long fileSize, String metaJson);
}
