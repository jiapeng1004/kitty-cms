package icu.jiapeng.kitty.material.storage.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.resource.constants.FileEngineTypeEnum;
import icu.jiapeng.kitty.material.resource.constants.FileStorageInstanceCodeEnum;
import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;
import icu.jiapeng.kitty.material.resource.mapper.KtFileStorageMapper;
import icu.jiapeng.kitty.material.support.cache.StringRedisTemplateCacheOperator;
import icu.jiapeng.kitty.material.storage.StorageDriver;
import icu.jiapeng.kitty.material.storage.StorageDriverFactory;
import icu.jiapeng.kitty.material.storage.StorageConnectivityResult;
import icu.jiapeng.kitty.material.storage.StorageRouteRequest;
import icu.jiapeng.kitty.material.storage.StorageRouteResult;
import icu.jiapeng.kitty.material.storage.dto.MaterialFileStorageUpsertDTO;
import icu.jiapeng.kitty.material.storage.dto.MaterialStorageRoutePreviewDTO;
import icu.jiapeng.kitty.material.storage.vo.MaterialFileStorageVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageInstanceOptionVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageConnectivityVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageObjectKeyNormalizeVO;
import icu.jiapeng.kitty.material.storage.redis.MaterialStorageRedisPubSubChannels;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageRoutePreviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * KtFileStorage服务类：管理存储配置，提供存储路由、连通性检测等功能。
 */
@Service
@RequiredArgsConstructor
public class KtFileStorageServiceImpl extends ServiceImpl<KtFileStorageMapper, KtFileStorage> implements KtFileStorageService {

    private static final long STORAGE_ID_LIST_TTL_MINUTES = 1;

    private static final String CACHE_KEY_STORAGE_IDS = "material:storage:id:list";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** 自定义存储主键：首字符字母数字，总长 1–64，仅 [a-zA-Z0-9._-] */
    private static final Pattern STORAGE_ID_CUSTOM_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9._-]{0,62}$");

    private static final int STORAGE_ID_MAX_LEN = 64;

    private final KtFileStorageMapper fileStorageMapper;
    private final StorageDriverFactory storageDriverFactory;
    private final StringRedisTemplateCacheOperator cacheOperator;
    private final StringRedisTemplate stringRedisTemplate;

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

    @Override
    public List<MaterialStorageInstanceOptionVO> listStorageInstanceOptions() {
        return Arrays.stream(FileStorageInstanceCodeEnum.values())
                .map(e -> {
                    MaterialStorageInstanceOptionVO vo = new MaterialStorageInstanceOptionVO();
                    vo.setStorageType(e.getStorageType());
                    vo.setCode(e.getCode());
                    vo.setLabel(e.getLabel());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<MaterialFileStorageVO> listFileStorageConfigs() {
        List<KtFileStorage> list = fileStorageMapper.selectList(new LambdaQueryWrapper<KtFileStorage>().orderByDesc(KtFileStorage::getUpdateTime));
        return list.stream().map(this::toMaterialFileStorageVo).collect(Collectors.toList());
    }

    @Override
    public MaterialFileStorageVO createFileStorage(MaterialFileStorageUpsertDTO req) {
        validateEngineType(req.getStorageType());
        if (!StringUtils.hasText(req.getSecretKey()) && FileEngineTypeEnum.OBJECT_STORAGE.getType().equals(req.getStorageType())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String id = resolveNewStorageId(req.getId());
        KtFileStorage e = new KtFileStorage();
        e.setId(id);
        fillEntityFromUpsert(e, req, true);
        applyPrimaryExclusiveBeforePersist(e.getId(), Boolean.TRUE.equals(e.getPrimaryFlag()));
        save(e);
        evictStorageListCache();
        return toMaterialFileStorageVo(getById(e.getId()));
    }

    @Override
    public MaterialFileStorageVO updateFileStorage(MaterialFileStorageUpsertDTO req) {
        if (!StringUtils.hasText(req.getId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        validateEngineType(req.getStorageType());
        KtFileStorage existing = getById(req.getId().trim());
        if (existing == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        fillEntityFromUpsert(existing, req, false);
        applyPrimaryExclusiveBeforePersist(existing.getId(), Boolean.TRUE.equals(existing.getPrimaryFlag()));
        updateById(existing);
        evictStorageListCache();
        publishS3ClientCacheInvalidate(existing.getId());
        return toMaterialFileStorageVo(getById(existing.getId()));
    }

    @Override
    public void deleteFileStorage(String storageId) {
        if (!StringUtils.hasText(storageId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String sid = storageId.trim();
        removeById(sid);
        evictStorageListCache();
        publishS3ClientCacheInvalidate(sid);
    }

    @Override
    public String requirePrimaryStorageId() {
        KtFileStorage row = lambdaQuery()
                .eq(KtFileStorage::getPrimaryFlag, true)
                .last("LIMIT 1")
                .one();
        if (row == null) {
            throw BizException.of(ResultStatus.MATERIAL_PRIMARY_STORAGE_NOT_SET);
        }
        return row.getId();
    }

    /**
     * 将本条设为主存储前，先清除其它行的主存储标记。
     */
    private void applyPrimaryExclusiveBeforePersist(String thisId, boolean thisAsPrimary) {
        if (!thisAsPrimary) {
            return;
        }
        lambdaUpdate()
                .set(KtFileStorage::getPrimaryFlag, false)
                .ne(KtFileStorage::getId, thisId.trim())
                .update();
    }

    private void validateEngineType(String storageType) {
        if (!StringUtils.hasText(storageType) || FileEngineTypeEnum.getByType(storageType.trim()).isEmpty()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
    }

    /**
     * 新建：请求未带 id 时用 SecureRandom 生成 32 位十六进制主键；带 id 则校验格式与唯一性。
     */
    private String resolveNewStorageId(String requestedId) {
        String trimmed = StrUtil.trimToNull(requestedId);
        if (!StringUtils.hasText(trimmed)) {
            String id;
            do {
                id = randomHexStorageId();
            } while (getById(id) != null);
            return id;
        }
        validateCustomStorageId(trimmed);
        if (getById(trimmed) != null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        return trimmed;
    }

    private static String randomHexStorageId() {
        byte[] b = new byte[16];
        SECURE_RANDOM.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }

    private void validateCustomStorageId(String id) {
        if (id.length() > STORAGE_ID_MAX_LEN || !STORAGE_ID_CUSTOM_PATTERN.matcher(id).matches()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
    }

    private void fillEntityFromUpsert(KtFileStorage e, MaterialFileStorageUpsertDTO req, boolean creating) {
        e.setStorageType(req.getStorageType().trim());
        e.setBucket(StrUtil.trimToNull(req.getBucket()));
        e.setInternalEndpoint(StrUtil.trimToNull(req.getInternalEndpoint()));
        e.setExternalEndpoint(StrUtil.trimToNull(req.getExternalEndpoint()));
        if (creating) {
            e.setAccessKey(StrUtil.trimToNull(req.getAccessKey()));
            e.setSecretKey(req.getSecretKey());
            e.setPrimaryFlag(Boolean.TRUE.equals(req.getPrimaryStorage()));
        } else {
            if (StringUtils.hasText(req.getAccessKey())) {
                e.setAccessKey(req.getAccessKey().trim());
            }
            if (StringUtils.hasText(req.getSecretKey())) {
                e.setSecretKey(req.getSecretKey());
            }
            if (req.getPrimaryStorage() != null) {
                e.setPrimaryFlag(req.getPrimaryStorage());
            }
        }
    }

    private MaterialFileStorageVO toMaterialFileStorageVo(KtFileStorage e) {
        if (e == null) {
            return null;
        }
        MaterialFileStorageVO vo = new MaterialFileStorageVO();
        vo.setId(e.getId());
        vo.setStorageType(e.getStorageType());
        vo.setBucket(e.getBucket());
        vo.setInternalEndpoint(e.getInternalEndpoint());
        vo.setExternalEndpoint(e.getExternalEndpoint());
        vo.setAccessKey(StrUtil.trimToNull(e.getAccessKey()));
        vo.setSecretKey(e.getSecretKey());
        vo.setSecretConfigured(StringUtils.hasText(e.getSecretKey()));
        vo.setPrimaryStorage(Boolean.TRUE.equals(e.getPrimaryFlag()));
        return vo;
    }

    private void evictStorageListCache() {
        try {
            cacheOperator.delete(CACHE_KEY_STORAGE_IDS);
        } catch (Exception ignored) {
        }
    }

    /**
     * 通知各 Pod 驱逐该存储 id 下缓存的 S3 客户端（与磁盘型存储无关时订阅端 no-op）。
     */
    private void publishS3ClientCacheInvalidate(String storageId) {
        if (!StringUtils.hasText(storageId)) {
            return;
        }
        try {
            stringRedisTemplate.convertAndSend(MaterialStorageRedisPubSubChannels.S3_STORAGE_CLIENT_INVALIDATE, storageId.trim());
        } catch (Exception ignored) {
        }
    }
}
