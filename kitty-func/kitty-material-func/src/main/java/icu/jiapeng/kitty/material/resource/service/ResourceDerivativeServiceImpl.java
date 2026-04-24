package icu.jiapeng.kitty.material.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.material.resource.entity.KtResourceDerivative;
import icu.jiapeng.kitty.material.resource.mapper.KtResourceDerivativeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResourceDerivativeServiceImpl extends ServiceImpl<KtResourceDerivativeMapper, KtResourceDerivative>
        implements ResourceDerivativeService {

    @Override
    public List<KtResourceDerivative> listByResourceId(String resourceId) {
        return list(new LambdaQueryWrapper<KtResourceDerivative>()
                .eq(KtResourceDerivative::getResourceId, resourceId)
                .orderByDesc(KtResourceDerivative::getUpdateTime));
    }

    @Override
    public List<KtResourceDerivative> listByResourceIdsAndDestinationType(Iterable<String> resourceIds, String destinationType) {
        if (resourceIds == null || !StringUtils.hasText(destinationType)) {
            return List.of();
        }
        var ids = new java.util.ArrayList<String>();
        for (String id : resourceIds) {
            if (StringUtils.hasText(id)) {
                ids.add(id.trim());
            }
        }
        if (ids.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<KtResourceDerivative>()
                .in(KtResourceDerivative::getResourceId, ids)
                .eq(KtResourceDerivative::getDestinationType, destinationType.trim()));
    }

    @Override
    public Optional<KtResourceDerivative> findByResourceAndType(String resourceId, String destinationType) {
        return Optional.ofNullable(getOne(
                new LambdaQueryWrapper<KtResourceDerivative>()
                        .eq(KtResourceDerivative::getResourceId, resourceId)
                        .eq(KtResourceDerivative::getDestinationType, destinationType)
                        .last("limit 1")));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertExternal(String resourceId, String destinationType, String externalUrl, Long fileSize, String metaJson) {
        Optional<KtResourceDerivative> opt = findByResourceAndType(resourceId, destinationType);
        KtResourceDerivative row = opt.orElseGet(() -> {
            KtResourceDerivative n = new KtResourceDerivative();
            n.setId(UUID.randomUUID().toString());
            n.setResourceId(resourceId);
            n.setDestinationType(destinationType);
            return n;
        });
        row.setExternalUrl(externalUrl);
        row.setFileSize(fileSize);
        row.setMetaJson(metaJson);
        saveOrUpdate(row);
    }
}
