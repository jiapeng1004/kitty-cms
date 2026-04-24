package icu.jiapeng.kitty.material.transcode.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.catalog.service.CatalogService;
import icu.jiapeng.kitty.material.catalog.constants.CatalogPermission;
import icu.jiapeng.kitty.material.resource.entity.KtResource;
import icu.jiapeng.kitty.material.transcode.TranscodePlatforms;
import icu.jiapeng.kitty.material.transcode.dto.CatalogTranscodeBindCreateDTO;
import icu.jiapeng.kitty.material.transcode.dto.MaterialTranscodeStrategyUpsertDTO;
import icu.jiapeng.kitty.material.transcode.entity.KtCatalogTranscodeStrategyBind;
import icu.jiapeng.kitty.material.transcode.entity.KtMaterialTranscodeStrategy;
import icu.jiapeng.kitty.material.transcode.model.TranscodeStrategyResolution;
import icu.jiapeng.kitty.material.transcode.model.TranscodeStrategySource;
import icu.jiapeng.kitty.material.transcode.vo.CatalogTranscodeBindVO;
import icu.jiapeng.kitty.material.transcode.vo.MaterialTranscodeStrategyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterialTranscodeStrategyFacade {

    private final MaterialTranscodeStrategyService strategyService;
    private final CatalogTranscodeStrategyBindService bindService;
    private final CatalogService catalogService;

    public List<MaterialTranscodeStrategyVO> listStrategies(Integer resourceType) {
        LambdaQueryWrapper<KtMaterialTranscodeStrategy> w = new LambdaQueryWrapper<>();
        if (resourceType != null) {
            w.eq(KtMaterialTranscodeStrategy::getResourceType, resourceType);
        }
        w.orderByDesc(KtMaterialTranscodeStrategy::getUpdateTime);
        return strategyService.list(w).stream()
                .map(this::toStrategyVo)
                .toList();
    }

    public MaterialTranscodeStrategyVO createStrategy(MaterialTranscodeStrategyUpsertDTO req) {
        validateStrategyUpsert(req, false);
        validateGlobalDefaultSemantics(req, null);
        KtMaterialTranscodeStrategy s = new KtMaterialTranscodeStrategy();
        s.setId(UUID.randomUUID().toString());
        applyStrategy(s, req);
        clearOtherGlobalDefaultsIfNeeded(s, null);
        strategyService.save(s);
        return toStrategyVo(s);
    }

    public MaterialTranscodeStrategyVO updateStrategy(MaterialTranscodeStrategyUpsertDTO req) {
        validateStrategyUpsert(req, true);
        KtMaterialTranscodeStrategy existing = strategyService.getById(req.getId());
        if (existing == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        validateGlobalDefaultSemantics(req, existing);
        applyStrategy(existing, req);
        clearOtherGlobalDefaultsIfNeeded(existing, existing.getId());
        strategyService.updateById(existing);
        return toStrategyVo(existing);
    }

    public void deleteStrategy(String id) {
        if (isBlank(id)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        long ref = bindService.lambdaQuery()
                .eq(KtCatalogTranscodeStrategyBind::getStrategyId, id)
                .count();
        if (ref > 0) {
            throw new BizException("该策略仍被栏目绑定引用，请先解除绑定", ResultStatus.NORMAL_ERROR);
        }
        strategyService.removeById(id);
    }

    public List<CatalogTranscodeBindVO> listBinds(String catalogId) {
        if (isBlank(catalogId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        catalogService.requireOnCatalog(catalogId, CatalogPermission.TRANSCODE_POLICY_MANAGE);
        return bindService.listByCatalogOrderSort(catalogId).stream().map(this::toBindVo).toList();
    }

    public CatalogTranscodeBindVO createBind(CatalogTranscodeBindCreateDTO req) {
        if (req == null || isBlank(req.getCatalogId()) || isBlank(req.getStrategyId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        catalogService.requireOnCatalog(req.getCatalogId(), CatalogPermission.TRANSCODE_POLICY_MANAGE);
        KtMaterialTranscodeStrategy strategy = strategyService.getById(req.getStrategyId());
        if (strategy == null || !Objects.equals(1, strategy.getEnabled())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (req.getResourceType() != null && strategy.getResourceType() != null
                && !req.getResourceType().equals(strategy.getResourceType())) {
            throw new BizException("策略资源类型与栏目绑定要求不一致", ResultStatus.PARAM_ERROR);
        }
        KtCatalogTranscodeStrategyBind b = new KtCatalogTranscodeStrategyBind();
        b.setId(UUID.randomUUID().toString());
        b.setCatalogId(req.getCatalogId());
        b.setStrategyId(req.getStrategyId());
        b.setResourceType(req.getResourceType());
        b.setSortNum(req.getSortNum() == null ? 0 : req.getSortNum());
        if (!bindService.save(b)) {
            throw BizException.of(ResultStatus.NORMAL_ERROR);
        }
        return toBindVo(b);
    }

    public void deleteBind(String bindId) {
        if (isBlank(bindId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtCatalogTranscodeStrategyBind b = bindService.getById(bindId);
        if (b == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        catalogService.requireOnCatalog(b.getCatalogId(), CatalogPermission.TRANSCODE_POLICY_MANAGE);
        bindService.removeById(bindId);
    }

    public Optional<KtMaterialTranscodeStrategy> getEnabledStrategyById(String id) {
        if (isBlank(id)) {
            return Optional.empty();
        }
        KtMaterialTranscodeStrategy s = strategyService.getById(id);
        if (s == null || !Objects.equals(1, s.getEnabled())) {
            return Optional.empty();
        }
        return Optional.of(s);
    }

    /**
     * 上传完成后的解析：显式 → 栏目绑定（按 sort） → 全局默认 → 无（不主转码）。
     * 当显式指定存在但不合法时抛出业务异常（合并/绑定流程应失败）。
     */
    public TranscodeStrategyResolution resolveForUpload(KtResource resource, String explicitStrategyId) {
        if (resource == null) {
            return TranscodeStrategyResolution.none();
        }
        if (!isBlank(explicitStrategyId)) {
            KtMaterialTranscodeStrategy s = strategyService.getById(explicitStrategyId.trim());
            if (s == null) {
                throw new BizException("转码策略不存在", ResultStatus.PARAM_ERROR);
            }
            if (!Objects.equals(1, s.getEnabled())) {
                throw new BizException("转码策略已禁用", ResultStatus.PARAM_ERROR);
            }
            if (s.getResourceType() != null && !s.getResourceType().equals(resource.getType())) {
                throw new BizException("显式转码策略与资源类型不匹配", ResultStatus.PARAM_ERROR);
            }
            return new TranscodeStrategyResolution(Optional.of(s), TranscodeStrategySource.EXPLICIT);
        }

        for (KtCatalogTranscodeStrategyBind bind : bindService.listByCatalogOrderSort(resource.getCatalogId())) {
            if (bind.getResourceType() != null && !bind.getResourceType().equals(resource.getType())) {
                continue;
            }
            KtMaterialTranscodeStrategy st = strategyService.getById(bind.getStrategyId());
            if (st == null || !Objects.equals(1, st.getEnabled())) {
                continue;
            }
            if (st.getResourceType() != null && !st.getResourceType().equals(resource.getType())) {
                continue;
            }
            return new TranscodeStrategyResolution(Optional.of(st), TranscodeStrategySource.CATALOG);
        }

        return findGlobalDefault(resource.getType())
                .map(s -> new TranscodeStrategyResolution(Optional.of(s), TranscodeStrategySource.GLOBAL))
                .orElse(TranscodeStrategyResolution.none());
    }

    /**
     * 手动入队时：可传 overrideStrategyId 覆盖解析链；当 resource 非空时校验策略与资源类型一致（若策略上绑定了资源类型）。
     */
    public Optional<KtMaterialTranscodeStrategy> resolveForResource(KtResource resource, String overrideStrategyId) {
        if (resource == null) {
            if (!isBlank(overrideStrategyId)) {
                return getEnabledStrategyById(overrideStrategyId);
            }
            return Optional.empty();
        }
        if (!isBlank(overrideStrategyId)) {
            return getEnabledStrategyById(overrideStrategyId)
                    .filter(s -> s.getResourceType() == null || s.getResourceType().equals(resource.getType()));
        }
        return resolveForUpload(resource, null).strategy();
    }

    private Optional<KtMaterialTranscodeStrategy> findGlobalDefault(Integer resourceType) {
        if (resourceType == null) {
            return Optional.empty();
        }
        KtMaterialTranscodeStrategy s = strategyService.getOne(
                new LambdaQueryWrapper<KtMaterialTranscodeStrategy>()
                        .eq(KtMaterialTranscodeStrategy::getIsGlobalDefault, 1)
                        .eq(KtMaterialTranscodeStrategy::getResourceType, resourceType)
                        .eq(KtMaterialTranscodeStrategy::getEnabled, 1)
                        .last("limit 1"));
        return Optional.ofNullable(s);
    }

    private void validateGlobalDefaultSemantics(MaterialTranscodeStrategyUpsertDTO req, KtMaterialTranscodeStrategy old) {
        if (req.getIsGlobalDefault() == null) {
            return;
        }
        if (req.getIsGlobalDefault() == 1) {
            if (req.getResourceType() == null) {
                throw new BizException("设全局默认时必须指定 resourceType", ResultStatus.PARAM_ERROR);
            }
        }
    }

    private void clearOtherGlobalDefaultsIfNeeded(KtMaterialTranscodeStrategy row, String excludeId) {
        if (!Objects.equals(1, row.getIsGlobalDefault()) || row.getResourceType() == null) {
            return;
        }
        strategyService.lambdaUpdate()
                .set(KtMaterialTranscodeStrategy::getIsGlobalDefault, 0)
                .eq(KtMaterialTranscodeStrategy::getResourceType, row.getResourceType())
                .eq(KtMaterialTranscodeStrategy::getIsGlobalDefault, 1)
                .ne(excludeId != null, KtMaterialTranscodeStrategy::getId, excludeId)
                .update();
    }

    private void validateStrategyUpsert(MaterialTranscodeStrategyUpsertDTO req, boolean update) {
        if (req == null || isBlank(req.getName()) || isBlank(req.getExternalStrategyId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (update && isBlank(req.getId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        try {
            Long.parseLong(req.getExternalStrategyId().trim());
        } catch (NumberFormatException e) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
    }

    private void applyStrategy(KtMaterialTranscodeStrategy s, MaterialTranscodeStrategyUpsertDTO req) {
        s.setName(req.getName());
        s.setPlatformCode(isBlank(req.getPlatformCode()) ? TranscodePlatforms.KITTY_TRANSCODER_GRPC : req.getPlatformCode().trim());
        s.setExternalStrategyId(req.getExternalStrategyId().trim());
        s.setParamsJson(req.getParamsJson());
        s.setEnabled(req.getEnabled() == null ? 1 : req.getEnabled());
        if (req.getResourceType() != null) {
            s.setResourceType(req.getResourceType());
        }
        if (req.getIsGlobalDefault() != null) {
            s.setIsGlobalDefault(req.getIsGlobalDefault() == 1 ? 1 : 0);
        } else if (s.getIsGlobalDefault() == null) {
            s.setIsGlobalDefault(0);
        }
    }

    private MaterialTranscodeStrategyVO toStrategyVo(KtMaterialTranscodeStrategy s) {
        MaterialTranscodeStrategyVO vo = new MaterialTranscodeStrategyVO();
        vo.setId(s.getId());
        vo.setName(s.getName());
        vo.setPlatformCode(s.getPlatformCode());
        vo.setExternalStrategyId(s.getExternalStrategyId());
        vo.setParamsJson(s.getParamsJson());
        vo.setEnabled(s.getEnabled());
        vo.setResourceType(s.getResourceType());
        vo.setIsGlobalDefault(s.getIsGlobalDefault());
        return vo;
    }

    private CatalogTranscodeBindVO toBindVo(KtCatalogTranscodeStrategyBind b) {
        CatalogTranscodeBindVO vo = new CatalogTranscodeBindVO();
        vo.setId(b.getId());
        vo.setCatalogId(b.getCatalogId());
        vo.setStrategyId(b.getStrategyId());
        KtMaterialTranscodeStrategy st = strategyService.getById(b.getStrategyId());
        if (st != null) {
            vo.setStrategyName(st.getName());
        }
        vo.setResourceType(b.getResourceType());
        vo.setSortNum(b.getSortNum());
        return vo;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
