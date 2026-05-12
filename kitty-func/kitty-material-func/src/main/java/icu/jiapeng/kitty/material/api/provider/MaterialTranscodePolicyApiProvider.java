package icu.jiapeng.kitty.material.api.provider;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import icu.jiapeng.kitty.material.transcode.api.MaterialTranscodePolicyApi;
import icu.jiapeng.kitty.material.transcode.dto.CatalogTranscodeBindCreateDTO;
import icu.jiapeng.kitty.material.transcode.dto.MaterialTranscodeStrategyUpsertDTO;
import icu.jiapeng.kitty.material.transcode.service.MaterialTranscodeStrategyFacade;
import icu.jiapeng.kitty.material.transcode.vo.CatalogTranscodeBindVO;
import icu.jiapeng.kitty.material.transcode.vo.MaterialTranscodeStrategyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 与 {@link icu.jiapeng.kitty.material.transcode.controller.MaterialTranscodePolicyController} 同逻辑；
 * 与 Controller 通过 profile {@code kitty-mam-embedded-api} 互斥。
 */
@Primary
@Service
@RequiredArgsConstructor
@Validated
public class MaterialTranscodePolicyApiProvider implements MaterialTranscodePolicyApi {

    private final MaterialTranscodeStrategyFacade transcodeStrategyService;

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_TRANSCODE_POLICY_MANAGE)
    public List<MaterialTranscodeStrategyVO> listStrategies(Integer resourceType) {
        return transcodeStrategyService.listStrategies(resourceType);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_TRANSCODE_POLICY_MANAGE)
    public MaterialTranscodeStrategyVO createStrategy(MaterialTranscodeStrategyUpsertDTO req) {
        return transcodeStrategyService.createStrategy(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_TRANSCODE_POLICY_MANAGE)
    public MaterialTranscodeStrategyVO updateStrategy(MaterialTranscodeStrategyUpsertDTO req) {
        return transcodeStrategyService.updateStrategy(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_TRANSCODE_POLICY_MANAGE)
    public void deleteStrategy(String id) {
        transcodeStrategyService.deleteStrategy(id);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_TRANSCODE_POLICY_MANAGE)
    public List<CatalogTranscodeBindVO> listBinds(String catalogId) {
        return transcodeStrategyService.listBinds(catalogId);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_TRANSCODE_POLICY_MANAGE)
    public CatalogTranscodeBindVO createBind(CatalogTranscodeBindCreateDTO req) {
        return transcodeStrategyService.createBind(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_TRANSCODE_POLICY_MANAGE)
    public void deleteBind(String id) {
        transcodeStrategyService.deleteBind(id);
    }
}
