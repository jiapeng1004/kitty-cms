package icu.jiapeng.kitty.material.transcode.service;

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

    public List<MaterialTranscodeStrategyVO> listStrategies() {
        return strategyService.list().stream()
                .map(this::toStrategyVo)
                .toList();
    }

    public MaterialTranscodeStrategyVO createStrategy(MaterialTranscodeStrategyUpsertDTO req) {
        validateStrategyUpsert(req, false);
        KtMaterialTranscodeStrategy s = new KtMaterialTranscodeStrategy();
        s.setId(UUID.randomUUID().toString());
        applyStrategy(s, req);
        strategyService.save(s);
        return toStrategyVo(s);
    }

    public MaterialTranscodeStrategyVO updateStrategy(MaterialTranscodeStrategyUpsertDTO req) {
        validateStrategyUpsert(req, true);
        KtMaterialTranscodeStrategy existing = strategyService.getById(req.getId());
        if (existing == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        applyStrategy(existing, req);
        strategyService.save(existing);
        return toStrategyVo(existing);
    }

    public void deleteStrategy(String id) {
        if (isBlank(id)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
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
        if (strategy == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
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

    public Optional<KtMaterialTranscodeStrategy> resolveForResource(KtResource resource, String overrideStrategyId) {
        if (resource == null) {
            return Optional.empty();
        }
        if (!isBlank(overrideStrategyId)) {
            KtMaterialTranscodeStrategy s = strategyService.getById(overrideStrategyId);
            if (s != null && Objects.equals(1, s.getEnabled())) {
                return Optional.of(s);
            }
            return Optional.empty();
        }
        List<KtCatalogTranscodeStrategyBind> binds = bindService.listByCatalogOrderSort(resource.getCatalogId());
        for (KtCatalogTranscodeStrategyBind bind : binds) {
            if (bind.getResourceType() != null && !bind.getResourceType().equals(resource.getType())) {
                continue;
            }
            KtMaterialTranscodeStrategy st = strategyService.getById(bind.getStrategyId());
            if (st != null && Objects.equals(1, st.getEnabled())) {
                return Optional.of(st);
            }
        }
        return Optional.empty();
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
    }

    private MaterialTranscodeStrategyVO toStrategyVo(KtMaterialTranscodeStrategy s) {
        MaterialTranscodeStrategyVO vo = new MaterialTranscodeStrategyVO();
        vo.setId(s.getId());
        vo.setName(s.getName());
        vo.setPlatformCode(s.getPlatformCode());
        vo.setExternalStrategyId(s.getExternalStrategyId());
        vo.setParamsJson(s.getParamsJson());
        vo.setEnabled(s.getEnabled());
        return vo;
    }

    private CatalogTranscodeBindVO toBindVo(KtCatalogTranscodeStrategyBind b) {
        CatalogTranscodeBindVO vo = new CatalogTranscodeBindVO();
        vo.setId(b.getId());
        vo.setCatalogId(b.getCatalogId());
        vo.setStrategyId(b.getStrategyId());
        vo.setResourceType(b.getResourceType());
        vo.setSortNum(b.getSortNum());
        return vo;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
