package icu.jiapeng.kitty.material.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.catalog.service.CatalogService;
import icu.jiapeng.kitty.material.metadata.service.MaterialMetadataInstanceService;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataSnapshotVO;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import icu.jiapeng.kitty.material.resource.constants.ResourceTypeEnum;
import icu.jiapeng.kitty.material.resource.dto.*;
import icu.jiapeng.kitty.material.resource.entity.KtMetaFile;
import icu.jiapeng.kitty.material.resource.entity.KtResource;
import icu.jiapeng.kitty.material.resource.fingerprint.ResourceFingerprintSupport;
import icu.jiapeng.kitty.material.resource.mapper.KtResourceMapper;
import icu.jiapeng.kitty.material.resource.vo.MaterialMetaFileVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceDetailVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceFingerprintPrecheckVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceVO;
import icu.jiapeng.kitty.material.review.service.MaterialReviewService;
import icu.jiapeng.kitty.material.review.vo.MaterialReviewTaskVO;
import icu.jiapeng.kitty.material.searchsync.MaterialSearchQueryPort;
import icu.jiapeng.kitty.material.searchsync.service.MaterialSearchSyncTrigger;
import icu.jiapeng.kitty.material.task.ResourceTaskTypes;
import icu.jiapeng.kitty.material.task.service.MaterialResourceTaskService;
import icu.jiapeng.kitty.material.task.vo.MaterialResourceTaskVO;
import icu.jiapeng.kitty.material.embedding.KtEmbeddingRequest;
import icu.jiapeng.kitty.material.embedding.KtEmbeddingDTO;
import icu.jiapeng.kitty.material.embedding.KtEmbeddingPort;
import icu.jiapeng.kitty.material.embedding.MaterialVectorSourceKey;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialResourceServiceImpl extends ServiceImpl<KtResourceMapper, KtResource> implements MaterialResourceService {

    private static final String ROOT_PARENT_ID = "0";

    @Resource
    private MetaFileService metaFileService;
    @Resource
    private MetaFileStorageBindService metaFileStorageBindService;
    @Resource
    private CatalogService catalogService;
    @Resource
    private MaterialMetadataInstanceService metadataInstanceService;
    @Lazy
    @Resource
    private MaterialResourceTaskService materialResourceTaskService;
    @Resource
    private MaterialReviewService materialReviewService;
    @Resource
    private MaterialSearchSyncTrigger materialSearchSyncTrigger;
    @Resource
    private MaterialSearchQueryPort materialSearchQueryPort;
    @Resource
    private KtEmbeddingPort materialVectorEmbeddingPort;

    @Override
    public Optional<KtResource> findById(String id) {
        return Optional.ofNullable(getById(id));
    }

    @Override
    public List<KtResource> findAll() {
        return list();
    }

    @Override
    public KtResource saveResource(KtResource entity) {
        saveOrUpdate(entity);
        return entity;
    }

    @Override
    public KtResource createFolder(String title, String catalogId, String parentId) {
        if (!StringUtils.hasText(title) || !StringUtils.hasText(catalogId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String normalizedParentId = !StringUtils.hasText(parentId) ? ROOT_PARENT_ID : parentId;
        if (!ROOT_PARENT_ID.equals(normalizedParentId)) {
            KtResource parent = getById(normalizedParentId);
            if (parent == null) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            if (!ResourceTypeEnum.isFolder(parent.getType())) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
        }
        KtResource folder = new KtResource();
        folder.setId(UUID.randomUUID().toString());
        folder.setTitle(title);
        folder.setCatalogId(catalogId);
        folder.setParentId(normalizedParentId);
        folder.setType(ResourceTypeEnum.FOLDER.getType());
        save(folder);
        return getById(folder.getId());
    }

    @Override
    public void rebuildPaths(String catalogId) {
        // 由于已移除 path 字段，此方法不再需要实现
    }

    @Override
    public List<KtResource> planFolderUpload(String catalogId, String parentId, List<String> relativePaths) {
        if (!StringUtils.hasText(catalogId) || relativePaths == null || relativePaths.isEmpty()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String rootParentId = !StringUtils.hasText(parentId) ? ROOT_PARENT_ID : parentId;
        List<KtResource> all = list(
                new QueryWrapper<KtResource>().eq("catalog_id", catalogId)
        );
        Map<String, KtResource> byId = all.stream().collect(Collectors.toMap(KtResource::getId, Function.identity()));
        Map<String, KtResource> byParentAndTitle = all.stream()
                .collect(Collectors.toMap(item -> item.getParentId() + "::" + item.getTitle(), Function.identity(), (a, _) -> a));

        List<KtResource> created = new ArrayList<>();
        for (String raw : relativePaths) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            String normalized = raw.replace("\\", "/").trim();
            String[] parts = normalized.split("/");
            String currentParentId = rootParentId;
            for (int i = 0; i < parts.length; i++) {
                String name = parts[i].trim();
                if (name.isEmpty()) {
                    continue;
                }
                boolean isLast = i == parts.length - 1;
                boolean isFolder = !isLast || normalized.endsWith("/");
                String key = currentParentId + "::" + name;
                KtResource existed = byParentAndTitle.get(key);
                if (existed != null) {
                    currentParentId = existed.getId();
                    continue;
                }
                KtResource node = new KtResource();
                node.setId(UUID.randomUUID().toString());
                node.setTitle(name);
                node.setCatalogId(catalogId);
                node.setParentId(currentParentId);
                node.setType(isFolder ? ResourceTypeEnum.FOLDER.getType() : inferType(name).getType());
                save(node);
                byId.put(node.getId(), node);
                byParentAndTitle.put(key, node);
                created.add(node);
                currentParentId = node.getId();
            }
        }
        return created;
    }

    @Override
    public MaterialResourceDetailVO detail(String resourceId) {
        if (!StringUtils.hasText(resourceId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = findById(resourceId)
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW);
        MaterialResourceDetailVO vo = new MaterialResourceDetailVO();
        vo.setResource(toVo(resource));
        List<MaterialMetadataSnapshotVO> metadataSnapshots =
                metadataInstanceService.snapshotsForResourceDetail(resourceId);
        vo.setMetadata(metadataSnapshots);
        List<MaterialResourceTaskVO> tasks = materialResourceTaskService.listByResource(resourceId);
        vo.setTasks(tasks);
        vo.setTranscodeTasks(filterTranscodeTasks(tasks));
        vo.setTaggingTasks(filterTaggingTasks(tasks));
        List<MaterialReviewTaskVO> reviewTasks = materialReviewService.listVoForResourceDetail(resourceId);
        vo.setReviewTasks(reviewTasks);
        return vo;
    }

    @Override
    public List<MaterialResourceVO> list(MaterialResourceListQueryDTO query) {
        int size = normalizeListLimit(query == null ? null : query.getLimit());
        if (query != null && StringUtils.hasText(query.getSemanticText())) {
            if (!StringUtils.hasText(query.getCatalogId())) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            catalogService.requireOnCatalog(query.getCatalogId(), MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW);
            KtEmbeddingRequest embedReq = KtEmbeddingRequest.builder()
                    .sourceKey(MaterialVectorSourceKey.none())
                    .text(query.getSemanticText().trim())
                    .resourceId(null)
                    .build();
            KtEmbeddingDTO emb = materialVectorEmbeddingPort.embed(embedReq);
            List<String> ids = materialSearchQueryPort.searchIdsByKnn(
                    emb.values(), query.getCatalogId(), query.getParentId(), size);
            return loadVosByIdsOrdered(ids);
        }
        if (query != null && StringUtils.hasText(query.getKeyword())) {
            if (!StringUtils.hasText(query.getCatalogId())) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            catalogService.requireOnCatalog(query.getCatalogId(), MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW);
            List<String> ids = materialSearchQueryPort.searchIdsByFullText(
                    query.getKeyword().trim(), query.getCatalogId(), query.getParentId(), size);
            return loadVosByIdsOrdered(ids);
        }
        if (query != null && StringUtils.hasText(query.getCatalogId())) {
            catalogService.requireOnCatalog(query.getCatalogId(), MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW);
        }
        return listDb(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialResourceVO create(MaterialResourceUpsertDTO req) {
        validate(req, false);
        catalogService.requireOnCatalog(req.getCatalogId(), MaterialPermissionCode.MATERIAL_RESOURCE_CREATE);
        KtResource resource = new KtResource();
        resource.setId(UUID.randomUUID().toString());
        resource.setTitle(req.getTitle());
        resource.setCatalogId(req.getCatalogId());
        resource.setParentId(normalizeParentId(req.getParentId()));
        resource.setType(req.getType());
        save(resource);
        KtResource latest = findById(resource.getId()).orElse(resource);
        materialSearchSyncTrigger.publishFullDocument(latest);
        return toVo(latest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialResourceVO update(MaterialResourceUpsertDTO req) {
        validate(req, true);
        KtResource resource = findById(req.getId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        String oldCatalogId = resource.getCatalogId();
        catalogService.requireOnCatalog(oldCatalogId, MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE);
        if (!oldCatalogId.equals(req.getCatalogId())) {
            catalogService.requireOnCatalog(req.getCatalogId(), MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE);
        }
        resource.setTitle(req.getTitle());
        resource.setCatalogId(req.getCatalogId());
        resource.setParentId(normalizeParentId(req.getParentId()));
        resource.setType(req.getType());
        save(resource);
        rebuildPaths(req.getCatalogId());
        return toVo(resource);
    }

    @Override
    public MaterialResourceVO createFolder(MaterialFolderCreateDTO req) {
        catalogService.requireOnCatalog(req.getCatalogId(), MaterialPermissionCode.MATERIAL_RESOURCE_CREATE);
        KtResource folder = createFolder(req.getTitle(), req.getCatalogId(), req.getParentId());
        KtResource latest = findById(folder.getId()).orElse(folder);
        materialSearchSyncTrigger.publishFullDocument(latest);
        return toVo(latest);
    }

    @Override
    public void rebuildPath(MaterialResourceListQueryDTO query) {
        // 由于已移除 path 字段，此方法不再需要实现
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MaterialResourceVO> planFolderUpload(MaterialFolderUploadPlanDTO req) {
        if (req == null || !StringUtils.hasText(req.getCatalogId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        catalogService.requireOnCatalog(req.getCatalogId(), MaterialPermissionCode.MATERIAL_RESOURCE_CREATE);
        List<KtResource> created = planFolderUpload(req.getCatalogId(), req.getParentId(), req.getRelativePaths());
        for (KtResource r : created) {
            findById(r.getId()).ifPresent(materialSearchSyncTrigger::publishFullDocument);
        }
        return created.stream().map(this::toVo).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialResourceVO saveFingerprint(MaterialResourceFingerprintDTO req) {
        if (req == null || !StringUtils.hasText(req.getResourceId()) || req.getFileSize() == null || req.getFileSize() < 0 || req.getChunkCrc32List() == null || req.getChunkCrc32List().isEmpty()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = findById(req.getResourceId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE);
        resource.setFileSize(req.getFileSize());
        resource.setFingerprint(ResourceFingerprintSupport.format(req.getFileSize(), req.getChunkCrc32List()));
        save(resource);
        KtResource latest = findById(resource.getId()).orElse(resource);
        materialSearchSyncTrigger.publishFullDocument(latest);
        return toVo(latest);
    }

    @Override
    public Optional<MaterialMetaFileVO> findMetaFileByResource(String resourceId) {
        if (!StringUtils.hasText(resourceId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = findById(resourceId).orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW);
        return metaFileService.findByResourceId(resourceId).map(this::toMetaFileVo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialMetaFileVO bindMetaFile(MaterialMetaFileBindDTO req) {
        if (req == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = findById(req.getResourceId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE);
        KtMetaFile meta = metaFileStorageBindService.bind(req.getResourceId(), req.getStorageId(), req.getObjectKey(), req.getName());
        MaterialMetaFileVO vo = toMetaFileVo(meta);
        KtResource latest = findById(req.getResourceId()).orElse(resource);
        materialSearchSyncTrigger.publishFullDocument(latest);
        materialResourceTaskService.tryAutoEnqueueAfterBind(req.getResourceId());
        return vo;
    }

    @Override
    public MaterialResourceFingerprintPrecheckVO precheckFingerprint(MaterialResourceFingerprintDTO req) {
        if (req == null || req.getFileSize() == null || req.getFileSize() < 0 || req.getChunkCrc32List() == null || req.getChunkCrc32List().isEmpty()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String fingerprint = ResourceFingerprintSupport.format(req.getFileSize(), req.getChunkCrc32List());
        List<MaterialResourceVO> matched = findAll().stream()
                .filter(item -> fingerprint.equals(item.getFingerprint()))
                .map(this::toVo)
                .toList();
        MaterialResourceFingerprintPrecheckVO vo = new MaterialResourceFingerprintPrecheckVO();
        vo.setHit(!matched.isEmpty());
        vo.setMatchedResources(matched);
        return vo;
    }

    private static List<MaterialResourceTaskVO> filterTranscodeTasks(List<MaterialResourceTaskVO> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }
        return tasks.stream()
                .filter(t -> ResourceTaskTypes.TRANSCODE.equals(t.getTaskType()))
                .collect(Collectors.toList());
    }

    private static List<MaterialResourceTaskVO> filterTaggingTasks(List<MaterialResourceTaskVO> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }
        return tasks.stream()
                .filter(t -> {
                    String tt = t.getTaskType();
                    if (tt == null) {
                        return false;
                    }
                    String lower = tt.toLowerCase();
                    return lower.contains("tag") || lower.contains("vector") || lower.contains("embedding");
                })
                .collect(Collectors.toList());
    }

    private static int normalizeListLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 20;
        }
        return Math.min(limit, 200);
    }

    private List<MaterialResourceVO> listDb(MaterialResourceListQueryDTO query) {
        return findAll().stream()
                .filter(resource -> query == null || !StringUtils.hasText(query.getCatalogId()) || query.getCatalogId().equals(resource.getCatalogId()))
                .filter(resource -> query == null || !StringUtils.hasText(query.getParentId()) || query.getParentId().equals(resource.getParentId()))
                .map(this::toVo)
                .toList();
    }

    private List<MaterialResourceVO> loadVosByIdsOrdered(List<String> ids) {
        List<MaterialResourceVO> out = new ArrayList<>();
        for (String id : ids) {
            findById(id).map(this::toVo).ifPresent(out::add);
        }
        return out;
    }

    private MaterialResourceVO toVo(KtResource resource) {
        MaterialResourceVO vo = new MaterialResourceVO();
        vo.setId(resource.getId());
        vo.setTitle(resource.getTitle());
        vo.setCatalogId(resource.getCatalogId());
        vo.setParentId(resource.getParentId());
        vo.setFileSize(resource.getFileSize());
        vo.setFingerprint(resource.getFingerprint());
        vo.setType(resource.getType());
        return vo;
    }

    private MaterialMetaFileVO toMetaFileVo(KtMetaFile meta) {
        MaterialMetaFileVO vo = new MaterialMetaFileVO();
        vo.setId(meta.getId());
        vo.setResourceId(meta.getResourceId());
        vo.setName(meta.getName());
        vo.setSize(meta.getSize());
        vo.setStorageId(meta.getStorageId());
        vo.setObjectKey(meta.getObjectKey());
        return vo;
    }

    private void validate(MaterialResourceUpsertDTO req, boolean update) {
        if (req == null || !StringUtils.hasText(req.getCatalogId()) || req.getType() == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (update && !StringUtils.hasText(req.getId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (ResourceTypeEnum.ofType(req.getType()).isEmpty()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
    }

    private String normalizeParentId(String parentId) {
        return !StringUtils.hasText(parentId) ? "0" : parentId;
    }

    private ResourceTypeEnum inferType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".mkv")) {
            return ResourceTypeEnum.VIDEO;
        }
        if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".flac")) {
            return ResourceTypeEnum.AUDIO;
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
            return ResourceTypeEnum.IMAGE;
        }
        if (lower.endsWith(".txt") || lower.endsWith(".md")) {
            return ResourceTypeEnum.TEXT;
        }
        return ResourceTypeEnum.OTHER;
    }
}
