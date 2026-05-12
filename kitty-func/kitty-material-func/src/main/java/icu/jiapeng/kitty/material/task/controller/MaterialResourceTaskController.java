package icu.jiapeng.kitty.material.task.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.task.service.MaterialResourceTaskService;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import icu.jiapeng.kitty.material.task.api.MaterialResourceTaskApi;
import icu.jiapeng.kitty.material.task.dto.MaterialTranscodeEnqueueDTO;
import icu.jiapeng.kitty.material.task.vo.MaterialResourceTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class MaterialResourceTaskController implements MaterialResourceTaskApi {

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
