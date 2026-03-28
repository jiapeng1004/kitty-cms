package icu.jiapeng.kitty.material.task.service;

import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.common.core.util.PathUtil;
import icu.jiapeng.kitty.material.catalog.service.CatalogService;
import icu.jiapeng.kitty.material.config.ConfigCenterGateway;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import icu.jiapeng.kitty.material.resource.entity.KtMetaFile;
import icu.jiapeng.kitty.material.resource.entity.KtResource;
import icu.jiapeng.kitty.material.resource.service.MaterialResourceService;
import icu.jiapeng.kitty.material.resource.service.MetaFileService;
import icu.jiapeng.kitty.material.support.lock.RedissonDistributedLockOperator;
import icu.jiapeng.kitty.material.task.ResourceTaskTypes;
import icu.jiapeng.kitty.material.task.dto.MaterialTranscodeEnqueueDTO;
import icu.jiapeng.kitty.material.task.entity.KtResourceTask;
import icu.jiapeng.kitty.material.task.vo.MaterialResourceTaskVO;
import icu.jiapeng.kitty.material.transcode.TranscodeDispatchGateway;
import icu.jiapeng.kitty.material.transcode.TranscodeSubmitCommand;
import icu.jiapeng.kitty.material.transcode.entity.KtMaterialTranscodeStrategy;
import icu.jiapeng.kitty.material.transcode.service.MaterialTranscodeStrategyFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialResourceTaskService {

    private static final String CFG_AUTO_AFTER_BIND = "transcode.auto-after-bind";
    private static final String CFG_HTTP_INPUT_BASE = "transcode.http-input-base";
    private static final long RETRY_LOCK_WAIT_MS = 10_000L;
    private static final long RETRY_LOCK_LEASE_SECONDS = 120L;

    private final MaterialResourceService resourceService;
    private final MetaFileService metaFileService;
    private final ResourceTaskService resourceTaskService;
    private final MaterialTranscodeStrategyFacade transcodeStrategyFacade;
    private final TranscodeDispatchGateway transcodeDispatchGateway;
    private final ConfigCenterGateway configCenterGateway;
    private final CatalogService catalogService;
    private final RedissonDistributedLockOperator distributedLockOperator;

    /**
     * 根据资源ID列出任务
     *
     * @param resourceId 资源ID
     * @return 任务列表
     */
    public List<MaterialResourceTaskVO> listByResource(String resourceId) {
        if (!StringUtils.hasText(resourceId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = resourceService.findById(resourceId)
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW);
        return resourceTaskService.findVisibleByResourceId(resourceId).stream().map(this::toVo).toList();
    }

    /**
     * 入队转码任务
     *
     * @param dto 转码请求
     * @return 转码任务VO
     */
    public MaterialResourceTaskVO enqueueTranscode(MaterialTranscodeEnqueueDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getResourceId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = resourceService.findById(dto.getResourceId().trim())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE);
        KtMaterialTranscodeStrategy strategy = transcodeStrategyFacade
                .resolveForResource(resource, dto.getStrategyId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        String inputType = !StringUtils.hasText(dto.getInputType()) ? "HTTP" : dto.getInputType().trim();
        String inputPath = dto.getInputPath();
        if (!StringUtils.hasText(inputPath)) {
            inputPath = buildHttpInputFromMeta(resource.getId()).orElse(null);
        }
        if (!StringUtils.hasText(inputPath)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        long extSid = Long.parseLong(strategy.getExternalStrategyId().trim());
        int pri = dto.getPriority() == null ? 5 : Math.max(1, Math.min(10, dto.getPriority()));
        Optional<String> extTaskId = transcodeDispatchGateway.submit(
                strategy.getPlatformCode(),
                TranscodeSubmitCommand.builder()
                        .inputType(inputType)
                        .inputPath(inputPath.trim())
                        .externalStrategyId(extSid)
                        .priority(pri)
                        .build());
        if (extTaskId.isEmpty()) {
            throw BizException.of(ResultStatus.NORMAL_ERROR);
        }
        KtResourceTask row = new KtResourceTask();
        row.setId(UUID.randomUUID().toString());
        row.setResourceId(resource.getId());
        row.setResourceTitle(resource.getTitle());
        row.setTaskType(ResourceTaskTypes.TRANSCODE);
        row.setThirdTaskId(extTaskId.get());
        row.setProgress(0);
        row.setStatus("pending");
        row.setInputType(inputType);
        row.setInputPath(inputPath.trim());
        row.setMaterialStrategyId(strategy.getId());
        row.setDeleted(0);
        if (!resourceTaskService.save(row)) {
            throw BizException.of(ResultStatus.NORMAL_ERROR);
        }
        return toVo(row);
    }

    /**
     * 重试转码任务
     *
     * @param taskId 任务ID
     * @return 转码任务VO
     */
    public MaterialResourceTaskVO retryTranscode(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String tid = taskId.trim();
        String lockKey = "material:lock:task:retry:" + tid;
        MaterialResourceTaskVO[] box = new MaterialResourceTaskVO[1];
        distributedLockOperator.executeWithLock(lockKey, RETRY_LOCK_WAIT_MS, RETRY_LOCK_LEASE_SECONDS, () -> {
            log.info("material transcode retry start taskId={}", tid);
            box[0] = retryTranscodeUnderLock(tid);
            log.info("material transcode retry done taskId={}", tid);
        });
        return box[0];
    }

    private MaterialResourceTaskVO retryTranscodeUnderLock(String taskId) {
        KtResourceTask task = resourceTaskService.findById(taskId)
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        if (!ResourceTaskTypes.TRANSCODE.equals(task.getTaskType())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = resourceService.findById(task.getResourceId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE);
        if (!"failed".equalsIgnoreCase(task.getStatus())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (!StringUtils.hasText(task.getMaterialStrategyId()) || !StringUtils.hasText(task.getInputPath())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtMaterialTranscodeStrategy st = transcodeStrategyFacade.resolveForResource(null, task.getMaterialStrategyId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        long ext = Long.parseLong(st.getExternalStrategyId().trim());
        String inType = !StringUtils.hasText(task.getInputType()) ? "HTTP" : task.getInputType();
        Optional<String> newExt = transcodeDispatchGateway.submit(
                st.getPlatformCode(),
                TranscodeSubmitCommand.builder()
                        .inputType(inType)
                        .inputPath(task.getInputPath())
                        .externalStrategyId(ext)
                        .priority(5)
                        .build());
        if (newExt.isEmpty()) {
            throw BizException.of(ResultStatus.NORMAL_ERROR);
        }
        task.setThirdTaskId(newExt.get());
        task.setStatus("pending");
        task.setProgress(0);
        task.setDeleted(0);
        if (!resourceTaskService.save(task)) {
            throw BizException.of(ResultStatus.NORMAL_ERROR);
        }
        return toVo(task);
    }

    /**
     * 绑定后尝试自动入队转码任务
     *
     * @param resourceId 资源ID
     */
    public void tryAutoEnqueueAfterBind(String resourceId) {
        Optional<String> flag = configCenterGateway.getString("material", CFG_AUTO_AFTER_BIND);
        if (flag.isEmpty() || !Boolean.parseBoolean(flag.get().trim())) {
            return;
        }
        KtResource r = resourceService.findById(resourceId).orElse(null);
        if (r == null || r.getType() == null) {
            return;
        }
        if (r.getType() != 1 && r.getType() != 2) {
            return;
        }
        try {
            MaterialTranscodeEnqueueDTO dto = new MaterialTranscodeEnqueueDTO();
            dto.setResourceId(resourceId);
            enqueueTranscode(dto);
        } catch (Exception e) {
            log.debug("auto transcode skipped resourceId={}", resourceId, e);
        }
    }

    private Optional<String> buildHttpInputFromMeta(String resourceId) {
        Optional<String> base = configCenterGateway.getString("material", CFG_HTTP_INPUT_BASE);
        if (base.isEmpty() || base.get().isBlank()) {
            return Optional.empty();
        }
        Optional<KtMetaFile> meta = metaFileService.findByResourceId(resourceId);
        if (meta.isEmpty()) {
            return Optional.empty();
        }
        String b = base.get().trim().replaceAll("/+$", "");
        String key = meta.get().getObjectKey() == null ? "" : meta.get().getObjectKey().replaceAll("^/+", "");
        return Optional.of(PathUtil.builderPath(b, key));
    }

    private MaterialResourceTaskVO toVo(KtResourceTask t) {
        MaterialResourceTaskVO vo = new MaterialResourceTaskVO();
        vo.setId(t.getId());
        vo.setResourceId(t.getResourceId());
        vo.setResourceTitle(t.getResourceTitle());
        vo.setTaskType(t.getTaskType());
        vo.setThirdTaskId(t.getThirdTaskId());
        vo.setProgress(t.getProgress());
        vo.setStatus(t.getStatus());
        vo.setInputType(t.getInputType());
        vo.setInputPath(t.getInputPath());
        vo.setMaterialStrategyId(t.getMaterialStrategyId());
        if (StringUtils.hasText(t.getMaterialStrategyId())) {
            transcodeStrategyFacade.resolveForResource(null, t.getMaterialStrategyId()).ifPresent(s -> vo.setStrategyName(s.getName()));
        }
        return vo;
    }
}
