package icu.jiapeng.kitty.material.api.provider;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import icu.jiapeng.kitty.material.task.api.MaterialResourceTaskApi;
import icu.jiapeng.kitty.material.task.dto.MaterialTranscodeEnqueueDTO;
import icu.jiapeng.kitty.material.task.service.MaterialResourceTaskService;
import icu.jiapeng.kitty.material.task.vo.MaterialResourceTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 与 {@link icu.jiapeng.kitty.material.task.controller.MaterialResourceTaskController} 同逻辑；
 * 与 Controller 通过 profile {@code kitty-mam-embedded-api} 互斥。
 */
@Primary
@Service
@RequiredArgsConstructor
@Validated
public class MaterialResourceTaskApiProvider implements MaterialResourceTaskApi {

    private final MaterialResourceTaskService materialResourceTaskService;

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public List<MaterialResourceTaskVO> listByResource(String resourceId) {
        return materialResourceTaskService.listByResource(resourceId);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE)
    public MaterialResourceTaskVO enqueueTranscode(MaterialTranscodeEnqueueDTO req) {
        return materialResourceTaskService.enqueueTranscode(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE)
    public MaterialResourceTaskVO retryTranscode(String taskId) {
        return materialResourceTaskService.retryTranscode(taskId);
    }
}
