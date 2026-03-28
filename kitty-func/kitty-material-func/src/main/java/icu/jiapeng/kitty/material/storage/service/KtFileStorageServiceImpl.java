package icu.jiapeng.kitty.material.storage.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;
import icu.jiapeng.kitty.material.resource.mapper.KtFileStorageMapper;
import icu.jiapeng.kitty.material.support.cache.StringRedisTemplateCacheOperator;
import icu.jiapeng.kitty.material.storage.StorageDriver;
import icu.jiapeng.kitty.material.storage.StorageDriverFactory;
import icu.jiapeng.kitty.material.storage.StorageConnectivityResult;
import icu.jiapeng.kitty.material.storage.StorageRouteRequest;
import icu.jiapeng.kitty.material.storage.StorageRouteResult;
import icu.jiapeng.kitty.material.storage.dto.MaterialStorageRoutePreviewDTO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageConnectivityVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageObjectKeyNormalizeVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageRoutePreviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * KtFileStorage服务类：管理存储配置，提供存储路由、连通性检测等功能。
 */
@Service
@RequiredArgsConstructor
public class KtFileStorageServiceImpl extends ServiceImpl<KtFileStorageMapper, KtFileStorage> implements KtFileStorageService {

    private static final long STORAGE_ID_LIST_TTL_MINUTES = 1;

    private static final String CACHE_KEY_STORAGE_IDS = "material:storage:id:list";

    private final KtFileStorageMapper fileStorageMapper;
    private final StorageDriverFactory storageDriverFactory;
    private final StringRedisTemplateCacheOperator cacheOperator;

    /**
     * 返回当前库中全部存储记录 ID（无权限细分，供下拉选择等）。
     */
    @Override
    public List<String> listStorageIds() {
        String cached = cacheOperator.get(CACHE_KEY_STORAGE_IDS);
        if (cached != null && !cached.isBlank()) {
            try {
                return JSON.parseArray(cached, String.class);
            } catch (Exception ignored) {
            }
        }
        List<KtFileStorage> storageList = fileStorageMapper.selectList(null);
        List<String> ids = storageList.stream()
                .map(KtFileStorage::getId)
                .toList();
        try {
            cacheOperator.set(CACHE_KEY_STORAGE_IDS, JSON.toJSONString(ids), STORAGE_ID_LIST_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception ignored) {
        }
        return ids;
    }

    /**
     * 按驱动规则解析对象键并返回路由目标描述（如本地路径前缀、OSS 说明等），用于上传前确认。
     */
    @Override
    public MaterialStorageRoutePreviewVO previewRoute(MaterialStorageRoutePreviewDTO req) {
        if (req == null || StrUtil.isBlank(req.getStorageId()) || StrUtil.isBlank(req.getObjectKey())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtFileStorage storage = fileStorageMapper.selectById(req.getStorageId());
        if (storage == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        // TODO: 改为通过driverName获取驱动
        StorageDriver driver = storageDriverFactory.resolve(storage.getStorageType());
        StorageRouteRequest routeReq = new StorageRouteRequest();
        routeReq.setStorageId(storage.getId());
        routeReq.setObjectKey(driver.normalizeObjectKey(req.getObjectKey()));
        StorageRouteResult routeResult = driver.route(routeReq, storage);
        MaterialStorageRoutePreviewVO vo = new MaterialStorageRoutePreviewVO();
        vo.setStorageId(routeResult.getStorageId());
        vo.setDriverName(routeResult.getDriverName());
        vo.setObjectKey(routeResult.getObjectKey());
        vo.setRouteTarget(routeResult.getRouteTarget());
        return vo;
    }

    /**
     * 校验并规范化对象键（不校验存储是否存在以外的业务语义）。
     */
    @Override
    public MaterialStorageObjectKeyNormalizeVO normalizeObjectKey(MaterialStorageRoutePreviewDTO req) {
        if (req == null || StrUtil.isBlank(req.getStorageId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtFileStorage storage = fileStorageMapper.selectById(req.getStorageId());
        if (storage == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        StorageDriver driver = storageDriverFactory.resolve(storage.getStorageType());
        String normalized = driver.normalizeObjectKey(req.getObjectKey());
        MaterialStorageObjectKeyNormalizeVO vo = new MaterialStorageObjectKeyNormalizeVO();
        vo.setOriginalKey(req.getObjectKey());
        vo.setNormalizedKey(normalized);
        vo.setValid(driver.isValidObjectKey(normalized));
        vo.setDriverName(driver.driverName());
        return vo;
    }

    /**
     * 探测存储是否可达（依赖具体 {@link StorageDriver#testConnectivity} 实现，如磁盘路径存在、OSS 凭证等）。
     */
    @Override
    public MaterialStorageConnectivityVO checkConnectivity(MaterialStorageRoutePreviewDTO req) {
        if (req == null || StrUtil.isBlank(req.getStorageId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtFileStorage storage = fileStorageMapper.selectById(req.getStorageId());
        if (storage == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        StorageDriver driver = storageDriverFactory.resolve(storage.getStorageType());
        StorageConnectivityResult checked = driver.testConnectivity(storage.getId(), storage);
        MaterialStorageConnectivityVO vo = new MaterialStorageConnectivityVO();
        vo.setStorageId(checked.getStorageId());
        vo.setEngineType(checked.getEngineType());
        vo.setDriverName(checked.getDriverName());
        vo.setReachable(checked.getReachable());
        vo.setDetail(checked.getDetail());
        return vo;
    }
}
