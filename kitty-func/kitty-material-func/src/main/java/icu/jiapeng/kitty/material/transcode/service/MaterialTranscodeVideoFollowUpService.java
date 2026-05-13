package icu.jiapeng.kitty.material.transcode.service;

import icu.jiapeng.kitty.common.core.util.PathUtil;
import icu.jiapeng.kitty.material.config.MaterialTranscodeProperties;
import icu.jiapeng.kitty.material.resource.constants.ResourceDestinationTypes;
import icu.jiapeng.kitty.material.resource.constants.ResourceTypeEnum;
import icu.jiapeng.kitty.material.resource.entity.KtMetaFile;
import icu.jiapeng.kitty.material.resource.entity.KtResource;
import icu.jiapeng.kitty.material.resource.mapper.KtResourceMapper;
import icu.jiapeng.kitty.material.resource.service.MetaFileService;
import icu.jiapeng.kitty.material.resource.service.ResourceDerivativeService;
import icu.jiapeng.kitty.material.task.ResourceTaskTypes;
import icu.jiapeng.kitty.material.task.entity.KtResourceTask;
import icu.jiapeng.kitty.material.task.service.ResourceTaskService;
import icu.jiapeng.kitty.material.transcode.entity.MaterialTranscodeMagicClient;
import icu.jiapeng.kitty.transcoder.api.MagicExtractFramesRequest;
import icu.jiapeng.kitty.transcoder.api.MagicImageConvertRequest;
import icu.jiapeng.kitty.transcoder.api.TaskVO;
import icu.jiapeng.kitty.user.api.internal.api.UserInternalConfigApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 视频上传后：抽封面（Magic）、登记雪碧图占位任务；与主转码链路解耦，失败仅记录日志。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialTranscodeVideoFollowUpService {

    private static final String CFG_HTTP_INPUT_BASE = "transcode.http-input-base";
    private static final ExecutorService EXEC = Executors.newFixedThreadPool(2);

    private final MaterialTranscodeMagicClient materialTranscodeMagicClient;
    private final MetaFileService metaFileService;
    private final ResourceDerivativeService resourceDerivativeService;
    private final ResourceTaskService resourceTaskService;
    private final UserInternalConfigApi userInternalConfigApi;
    private final MaterialTranscodeProperties materialTranscodeProperties;
    private final KtResourceMapper ktResourceMapper;

    public void schedulePostUpload(String resourceId) {
        if (!StringUtils.hasText(resourceId)) {
            return;
        }
        EXEC.execute(() -> runCoverAndSprite(resourceId.trim()));
    }

    private void runCoverAndSprite(String resourceId) {
        try {
            Optional<KtMetaFile> meta = metaFileService.findByResourceId(resourceId);
            if (meta.isEmpty()) {
                return;
            }
            Optional<String> input = buildHttpInputUrl(meta.get());
            if (input.isEmpty()) {
                log.debug("video follow-up skip: no http input resourceId={}", resourceId);
                return;
            }
            if (!StringUtils.hasText(materialTranscodeProperties.getTranscoderHttpBase())) {
                log.debug("video follow-up skip: transcoderHttpBase not configured resourceId={}", resourceId);
                return;
            }
            MagicExtractFramesRequest coverReq = new MagicExtractFramesRequest();
            coverReq.setInputType("HTTP");
            coverReq.setInputPath(input.get());
            coverReq.setFrameInterval(30);
            coverReq.setFrameCount(1);
            coverReq.setOutputFormat("jpg");
            TaskVO cover = materialTranscodeMagicClient.magicExtractFrames(coverReq);
            if (cover.getOutputHttpUrl() != null && !cover.getOutputHttpUrl().isBlank()) {
                resourceDerivativeService.upsertExternal(
                        resourceId,
                        ResourceDestinationTypes.COVER,
                        cover.getOutputHttpUrl(),
                        null,
                        null);
            } else {
                log.warn("magic extract cover no outputHttpUrl resourceId={} status={}", resourceId, cover.getStatus());
            }
            scheduleSpritePlaceholder(resourceId, input.get());
        } catch (Exception e) {
            log.warn("video follow-up failed resourceId={}", resourceId, e);
        }
    }

    private void scheduleSpritePlaceholder(String resourceId, String sourceHttpInput) {
        try {
            MagicImageConvertRequest spriteReq = new MagicImageConvertRequest();
            spriteReq.setInputType("HTTP");
            spriteReq.setInputPath(sourceHttpInput);
            spriteReq.setTargetFormat("webp");
            spriteReq.setQuality(80);
            spriteReq.setResize("320x");
            TaskVO sprite = materialTranscodeMagicClient.magicImageConvert(spriteReq);
            if (sprite.getOutputHttpUrl() != null && !sprite.getOutputHttpUrl().isBlank()) {
                resourceDerivativeService.upsertExternal(
                        resourceId,
                        ResourceDestinationTypes.SPRITE,
                        sprite.getOutputHttpUrl(),
                        null,
                        "{\"note\":\"derived from source http for strip placeholder\"}");
            } else {
                registerSpritePendingTask(resourceId);
            }
        } catch (Exception e) {
            log.debug("sprite magic optional fail resourceId={}", resourceId, e);
            registerSpritePendingTask(resourceId);
        }
    }

    private void registerSpritePendingTask(String resourceId) {
        String title = "";
        KtResource r = ktResourceMapper.selectById(resourceId);
        if (r != null && StringUtils.hasText(r.getTitle())) {
            title = r.getTitle();
        }
        KtResourceTask t = new KtResourceTask();
        t.setId(UUID.randomUUID().toString());
        t.setResourceId(resourceId);
        t.setResourceTitle(title);
        t.setTaskType(ResourceTaskTypes.VIDEO_SPRITE);
        t.setStatus("pending");
        t.setProgress(0);
        t.setDeleted(0);
        resourceTaskService.save(t);
    }

    private Optional<String> buildHttpInputUrl(KtMetaFile meta) {
        Optional<String> base = resolveHttpInputBase();
        if (base.isEmpty() || !StringUtils.hasText(meta.getObjectKey())) {
            return Optional.empty();
        }
        String b = base.get().trim().replaceAll("/+$", "");
        String key = meta.getObjectKey().replaceAll("^/+", "");
        return Optional.of(PathUtil.builderPath(b, key));
    }

    private Optional<String> resolveHttpInputBase() {
        Optional<String> fromCenter = userInternalConfigApi.getString("material", CFG_HTTP_INPUT_BASE);
        if (fromCenter.isPresent() && StringUtils.hasText(fromCenter.get())) {
            return Optional.of(fromCenter.get().trim());
        }
        if (StringUtils.hasText(materialTranscodeProperties.getHttpInputBase())) {
            return Optional.of(materialTranscodeProperties.getHttpInputBase().trim());
        }
        return Optional.empty();
    }

    /**
     * 在合并事务提交后调用：仅视频走封面/雪碧；资源类型由调用方保证。
     */
    public void scheduleIfVideo(KtResource resource) {
        if (resource == null || resource.getType() == null) {
            return;
        }
        if (!ResourceTypeEnum.VIDEO.getType().equals(resource.getType())) {
            return;
        }
        schedulePostUpload(resource.getId());
    }
}
