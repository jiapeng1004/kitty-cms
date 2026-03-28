package icu.jiapeng.kitty.material.review.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.review.service.MaterialReviewService;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import icu.jiapeng.kitty.material.review.api.MaterialReviewApi;
import icu.jiapeng.kitty.material.review.dto.MaterialReviewApproveDTO;
import icu.jiapeng.kitty.material.review.dto.MaterialReviewRejectDTO;
import icu.jiapeng.kitty.material.review.dto.MaterialReviewSubmitDTO;
import icu.jiapeng.kitty.material.review.vo.MaterialReviewTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通用审核任务。
 */
@RestController
@RequiredArgsConstructor
@Validated
public class MaterialReviewController implements MaterialReviewApi {

    private final MaterialReviewService materialReviewService;

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_REVIEW_SUBMIT)
    public MaterialReviewTaskVO submit(MaterialReviewSubmitDTO req) {
        return materialReviewService.submit(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_REVIEW_APPROVE)
    public MaterialReviewTaskVO approve(MaterialReviewApproveDTO req) {
        return materialReviewService.approve(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_REVIEW_APPROVE)
    public MaterialReviewTaskVO reject(MaterialReviewRejectDTO req) {
        return materialReviewService.reject(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public List<MaterialReviewTaskVO> query(String bizType, String bizId) {
        return materialReviewService.queryByBiz(bizType, bizId);
    }
}
