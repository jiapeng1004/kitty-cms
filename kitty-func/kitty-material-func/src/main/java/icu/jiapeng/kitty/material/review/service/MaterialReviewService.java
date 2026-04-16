package icu.jiapeng.kitty.material.review.service;

import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.catalog.constants.CatalogPermission;
import icu.jiapeng.kitty.material.catalog.service.CatalogService;
import icu.jiapeng.kitty.material.resource.entity.KtResource;
import icu.jiapeng.kitty.material.resource.service.MaterialResourceService;
import icu.jiapeng.kitty.material.review.ReviewTaskStatuses;
import icu.jiapeng.kitty.material.review.dto.MaterialReviewApproveDTO;
import icu.jiapeng.kitty.material.review.dto.MaterialReviewRejectDTO;
import icu.jiapeng.kitty.material.review.dto.MaterialReviewSubmitDTO;
import icu.jiapeng.kitty.material.review.entity.KtReviewTask;
import icu.jiapeng.kitty.material.review.vo.MaterialReviewTaskVO;
import icu.jiapeng.kitty.material.user.UserContextGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialReviewService {

    private final ReviewTaskService reviewTaskService;
    private final UserContextGateway userContextGateway;
    private final MaterialResourceService resourceService;
    private final CatalogService catalogService;

    private static final String BIZ_TYPE_RESOURCE = "RESOURCE";

    @Transactional(rollbackFor = Exception.class)
    public MaterialReviewTaskVO submit(MaterialReviewSubmitDTO req) {
        if (req == null || !StringUtils.hasText(req.bizType()) || !StringUtils.hasText(req.bizId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String bizType = req.bizType().trim();
        String bizId = req.bizId().trim();
        guardBizAccess(bizType, bizId, CatalogPermission.REVIEW_SUBMIT);
        if (reviewTaskService.existsPending(bizType, bizId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String uid = userContextGateway.currentUserId();
        KtReviewTask t = new KtReviewTask();
        t.setId(UUID.randomUUID().toString());
        t.setBizType(bizType);
        t.setBizId(bizId);
        t.setSubmitUserId(uid);
        t.setReviewUserId(null);
        t.setStatus(ReviewTaskStatuses.PENDING);
        t.setReviewComment(null);
        Date now = new Date();
        t.setCreateTime(now);
        t.setUpdateTime(now);
        reviewTaskService.saveTask(t);
        log.info("material review submit bizType={} bizId={} taskId={} submitUser={}", bizType, bizId, t.getId(), uid);
        return toVo(t);
    }

    @Transactional(rollbackFor = Exception.class)
    public MaterialReviewTaskVO approve(MaterialReviewApproveDTO req) {
        if (req == null || !StringUtils.hasText(req.taskId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtReviewTask t = reviewTaskService.findById(req.taskId().trim())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        guardBizAccess(t.getBizType(), t.getBizId(), CatalogPermission.REVIEW_APPROVE);
        if (!ReviewTaskStatuses.PENDING.equals(t.getStatus())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String reviewer = userContextGateway.currentUserId();
        t.setReviewUserId(reviewer);
        t.setStatus(ReviewTaskStatuses.APPROVED);
        t.setReviewComment(StringUtils.hasText(req.reviewComment()) ? req.reviewComment().trim() : null);
        reviewTaskService.updateTask(t);
        log.info("material review approve taskId={} reviewer={} bizType={} bizId={}", t.getId(), reviewer, t.getBizType(), t.getBizId());
        return toVo(reviewTaskService.findById(t.getId()).orElse(t));
    }

    @Transactional(rollbackFor = Exception.class)
    public MaterialReviewTaskVO reject(MaterialReviewRejectDTO req) {
        if (req == null || !StringUtils.hasText(req.taskId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtReviewTask t = reviewTaskService.findById(req.taskId().trim())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        guardBizAccess(t.getBizType(), t.getBizId(), CatalogPermission.REVIEW_APPROVE);
        if (!ReviewTaskStatuses.PENDING.equals(t.getStatus())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String reviewer = userContextGateway.currentUserId();
        t.setReviewUserId(reviewer);
        t.setStatus(ReviewTaskStatuses.REJECTED);
        t.setReviewComment(StringUtils.hasText(req.reviewComment()) ? req.reviewComment().trim() : null);
        reviewTaskService.updateTask(t);
        log.info("material review reject taskId={} reviewer={} bizType={} bizId={}", t.getId(), reviewer, t.getBizType(), t.getBizId());
        return toVo(reviewTaskService.findById(t.getId()).orElse(t));
    }

    public List<MaterialReviewTaskVO> queryByBiz(String bizType, String bizId) {
        if (!StringUtils.hasText(bizType) || !StringUtils.hasText(bizId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String bt = bizType.trim();
        String bid = bizId.trim();
        guardBizAccess(bt, bid, CatalogPermission.RESOURCE_LIST_VIEW);
        return reviewTaskService.listByBizDesc(bt, bid).stream().map(this::toVo).collect(Collectors.toList());
    }

    public List<MaterialReviewTaskVO> listVoForResourceDetail(String resourceId) {
        if (!StringUtils.hasText(resourceId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource r = resourceService.findById(resourceId)
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(r.getCatalogId(), CatalogPermission.RESOURCE_LIST_VIEW);
        return reviewTaskService.listByBizDesc(BIZ_TYPE_RESOURCE, resourceId).stream().map(this::toVo).collect(Collectors.toList());
    }

    private void guardBizAccess(String bizType, String bizId, CatalogPermission permission) {
        if (BIZ_TYPE_RESOURCE.equals(bizType)) {
            KtResource r = resourceService.findById(bizId)
                    .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
            catalogService.requireOnCatalog(r.getCatalogId(), permission);
        }
    }

    private MaterialReviewTaskVO toVo(KtReviewTask t) {
        return new MaterialReviewTaskVO(
                t.getId(),
                t.getBizType(),
                t.getBizId(),
                t.getSubmitUserId(),
                t.getReviewUserId(),
                t.getStatus(),
                t.getReviewComment(),
                t.getCreateTime() != null ? t.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null,
                t.getUpdateTime() != null ? t.getUpdateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null
        );
    }
}
