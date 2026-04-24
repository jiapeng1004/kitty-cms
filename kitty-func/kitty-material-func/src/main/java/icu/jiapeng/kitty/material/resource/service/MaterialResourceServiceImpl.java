package icu.jiapeng.kitty.material.resource.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.material.catalog.service.CatalogService;
import icu.jiapeng.kitty.material.metadata.service.MaterialMetadataInstanceService;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataSnapshotVO;
import icu.jiapeng.kitty.material.catalog.constants.CatalogPermission;
import icu.jiapeng.kitty.material.behavior.MaterialDataEventClient;
import icu.jiapeng.kitty.material.resource.constants.ResourceDestinationTypes;
import icu.jiapeng.kitty.material.resource.constants.ResourceTypeEnum;
import icu.jiapeng.kitty.material.resource.dto.*;
import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;
import icu.jiapeng.kitty.material.resource.entity.KtMetaFile;
import icu.jiapeng.kitty.material.resource.entity.KtResource;
import icu.jiapeng.kitty.material.resource.entity.KtResourceDerivative;
import icu.jiapeng.kitty.material.resource.fingerprint.ResourceFingerprintSupport;
import icu.jiapeng.kitty.material.resource.mapper.KtFileStorageMapper;
import icu.jiapeng.kitty.material.resource.mapper.KtResourceMapper;
import icu.jiapeng.kitty.material.resource.support.MaterialResourcePreviewLinkBuilder;
import icu.jiapeng.kitty.material.resource.support.MaterialStoragePublicUrlBuilder;
import icu.jiapeng.kitty.material.resource.vo.MaterialDownloadUrlVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialMetaFileVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceDerivativeVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceDetailVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceFingerprintPrecheckVO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceVO;
import icu.jiapeng.kitty.material.user.UserContextGateway;
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
    @Resource
    private MaterialStoragePublicUrlBuilder materialStoragePublicUrlBuilder;
    @Resource
    private MaterialResourcePreviewLinkBuilder materialResourcePreviewLinkBuilder;
    @Resource
    private KtFileStorageMapper ktFileStorageMapper;
    @Resource
    private ResourceDerivativeService resourceDerivativeService;
    @Resource
    private UserContextGateway userContextGateway;
    @Resource
    private MaterialDataEventClient materialDataEventClient;

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
        String normalizedCatalogId = normalizeCatalogId(catalogId);
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
        folder.setCatalogId(normalizedCatalogId);
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
        String normalizedCatalogId = normalizeCatalogId(catalogId);
        String rootParentId = !StringUtils.hasText(parentId) ? ROOT_PARENT_ID : parentId;
        List<KtResource> all = list(
                new QueryWrapper<KtResource>().eq("catalog_id", normalizedCatalogId)
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
                node.setCatalogId(normalizedCatalogId);
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
        catalogService.requireOnCatalog(resource.getCatalogId(), CatalogPermission.RESOURCE_LIST_VIEW);
        MaterialResourceDetailVO vo = new MaterialResourceDetailVO();
        vo.setResource(enrichSingle(toVo(resource)));
        List<MaterialMetadataSnapshotVO> metadataSnapshots =
                metadataInstanceService.snapshotsForResourceDetail(resourceId);
        vo.setMetadata(metadataSnapshots);
        List<MaterialResourceTaskVO> tasks = materialResourceTaskService.listByResource(resourceId);
        vo.setTasks(tasks);
        vo.setTranscodeTasks(filterTranscodeTasks(tasks));
        vo.setTaggingTasks(filterTaggingTasks(tasks));
        List<MaterialReviewTaskVO> reviewTasks = materialReviewService.listVoForResourceDetail(resourceId);
        vo.setReviewTasks(reviewTasks);
        List<MaterialResourceDerivativeVO> derivVos = resourceDerivativeService.listByResourceId(resourceId).stream()
                .map(this::toDerivativeVo)
                .toList();
        vo.setDerivatives(derivVos);
        return vo;
    }

    @Override
    public List<MaterialResourceVO> list(MaterialResourceListQueryDTO query) {
        int size = normalizeListLimit(query == null ? null : query.getLimit());
        String normalizedCatalogId = query == null ? null : normalizeCatalogIdNullable(query.getCatalogId());
        List<MaterialResourceVO> result;
        if (query != null && StringUtils.hasText(query.getSemanticText())) {
            if (!StringUtils.hasText(normalizedCatalogId)) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            catalogService.requireOnCatalog(normalizedCatalogId, CatalogPermission.RESOURCE_LIST_VIEW);
            KtEmbeddingRequest embedReq = KtEmbeddingRequest.builder()
                    .sourceKey(MaterialVectorSourceKey.none())
                    .text(query.getSemanticText().trim())
                    .resourceId(null)
                    .build();
            KtEmbeddingDTO emb = materialVectorEmbeddingPort.embed(embedReq);
            List<String> ids = materialSearchQueryPort.searchIdsByKnn(
                    emb.values(), normalizedCatalogId, query.getParentId(), size);
            result = loadVosByIdsOrdered(ids);
        } else if (query != null && StringUtils.hasText(query.getKeyword())) {
            if (!StringUtils.hasText(normalizedCatalogId)) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            catalogService.requireOnCatalog(normalizedCatalogId, CatalogPermission.RESOURCE_LIST_VIEW);
            List<String> ids = materialSearchQueryPort.searchIdsByFullText(
                    query.getKeyword().trim(), normalizedCatalogId, query.getParentId(), size);
            result = loadVosByIdsOrdered(ids);
        } else {
            if (StringUtils.hasText(normalizedCatalogId)) {
                catalogService.requireOnCatalog(normalizedCatalogId, CatalogPermission.RESOURCE_LIST_VIEW);
            }
            result = listDb(query, normalizedCatalogId);
        }
        enrichSrcUrls(result);
        return result;
    }

    @Override
    public PageRespVo<MaterialResourceVO> page(MaterialResourceListPageQueryDTO query) {
        if (query == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        long page = Math.max(1L, query.getPage());
        long size = Math.min(200L, Math.max(1L, query.getSize()));
        String normalizedCatalogId = normalizeCatalogIdNullable(query.getCatalogId());
        if (!StringUtils.hasText(normalizedCatalogId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        catalogService.requireOnCatalog(normalizedCatalogId, CatalogPermission.RESOURCE_LIST_VIEW);

        if (StringUtils.hasText(query.getKeyword()) || StringUtils.hasText(query.getSemanticText())) {
            MaterialResourceListQueryDTO lq = new MaterialResourceListQueryDTO();
            lq.setCatalogId(query.getCatalogId());
            lq.setParentId(query.getParentId());
            lq.setKeyword(query.getKeyword());
            lq.setSemanticText(query.getSemanticText());
            int need = (int) Math.min(200L, page * size);
            lq.setLimit(Math.max(need, 1));
            List<MaterialResourceVO> fetched = list(lq);
            long total = fetched.size();
            int from = (int) ((page - 1) * size);
            List<MaterialResourceVO> records;
            if (from >= fetched.size()) {
                records = List.of();
            } else {
                int to = (int) Math.min(from + size, fetched.size());
                records = new ArrayList<>(fetched.subList(from, to));
            }
            return PageRespVo.<MaterialResourceVO>builder()
                    .page(page)
                    .size(size)
                    .total(total)
                    .records(records)
                    .build();
        }

        LambdaQueryWrapper<KtResource> w = new LambdaQueryWrapper<KtResource>()
                .eq(KtResource::getCatalogId, normalizedCatalogId);
        if (StringUtils.hasText(query.getParentId())) {
            w.eq(KtResource::getParentId, query.getParentId());
        }
        Page<KtResource> mpPage = new Page<>(page, size);
        Page<KtResource> mpResult = page(mpPage, w);
        List<MaterialResourceVO> records = mpResult.getRecords().stream().map(this::toVo).collect(Collectors.toList());
        enrichSrcUrls(records);
        return PageRespVo.<MaterialResourceVO>builder()
                .page(mpResult.getCurrent())
                .size(mpResult.getSize())
                .total(mpResult.getTotal())
                .records(records)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialResourceVO create(MaterialResourceUpsertDTO req) {
        validate(req, false);
        String normalizedCatalogId = normalizeCatalogId(req.getCatalogId());
        catalogService.requireOnCatalog(normalizedCatalogId, CatalogPermission.RESOURCE_CREATE);
        KtResource resource = new KtResource();
        resource.setId(UUID.randomUUID().toString());
        resource.setTitle(req.getTitle());
        resource.setCatalogId(normalizedCatalogId);
        resource.setParentId(normalizeParentId(req.getParentId()));
        resource.setType(req.getType());
        save(resource);
        KtResource latest = findById(resource.getId()).orElse(resource);
        materialSearchSyncTrigger.publishFullDocument(latest);
        return enrichSingle(toVo(latest));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialResourceVO update(MaterialResourceUpsertDTO req) {
        validate(req, true);
        KtResource resource = findById(req.getId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        String oldCatalogId = resource.getCatalogId();
        String normalizedCatalogId = normalizeCatalogId(req.getCatalogId());
        catalogService.requireOnCatalog(oldCatalogId, CatalogPermission.RESOURCE_UPDATE);
        if (!oldCatalogId.equals(normalizedCatalogId)) {
            catalogService.requireOnCatalog(normalizedCatalogId, CatalogPermission.RESOURCE_UPDATE);
        }
        resource.setTitle(req.getTitle());
        resource.setCatalogId(normalizedCatalogId);
        resource.setParentId(normalizeParentId(req.getParentId()));
        resource.setType(req.getType());
        save(resource);
        rebuildPaths(normalizedCatalogId);
        return enrichSingle(toVo(resource));
    }

    @Override
    public MaterialResourceVO createFolder(MaterialFolderCreateDTO req) {
        String normalizedCatalogId = normalizeCatalogId(req.getCatalogId());
        catalogService.requireOnCatalog(normalizedCatalogId, CatalogPermission.RESOURCE_CREATE);
        KtResource folder = createFolder(req.getTitle(), normalizedCatalogId, req.getParentId());
        KtResource latest = findById(folder.getId()).orElse(folder);
        materialSearchSyncTrigger.publishFullDocument(latest);
        return enrichSingle(toVo(latest));
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
        String normalizedCatalogId = normalizeCatalogId(req.getCatalogId());
        catalogService.requireOnCatalog(normalizedCatalogId, CatalogPermission.RESOURCE_CREATE);
        List<KtResource> created = planFolderUpload(normalizedCatalogId, req.getParentId(), req.getRelativePaths());
        for (KtResource r : created) {
            findById(r.getId()).ifPresent(materialSearchSyncTrigger::publishFullDocument);
        }
        List<MaterialResourceVO> out = created.stream().map(this::toVo).collect(Collectors.toCollection(ArrayList::new));
        enrichSrcUrls(out);
        return out;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialResourceVO saveFingerprint(MaterialResourceFingerprintDTO req) {
        if (req == null || !StringUtils.hasText(req.getResourceId()) || req.getFileSize() == null || req.getFileSize() < 0 || req.getChunkCrc32List() == null || req.getChunkCrc32List().isEmpty()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = findById(req.getResourceId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), CatalogPermission.RESOURCE_UPDATE);
        resource.setFileSize(req.getFileSize());
        resource.setFingerprint(ResourceFingerprintSupport.format(req.getFileSize(), req.getChunkCrc32List()));
        save(resource);
        KtResource latest = findById(resource.getId()).orElse(resource);
        materialSearchSyncTrigger.publishFullDocument(latest);
        return enrichSingle(toVo(latest));
    }

    @Override
    public Optional<MaterialMetaFileVO> findMetaFileByResource(String resourceId) {
        if (!StringUtils.hasText(resourceId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtResource resource = findById(resourceId).orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), CatalogPermission.RESOURCE_LIST_VIEW);
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
        catalogService.requireOnCatalog(resource.getCatalogId(), CatalogPermission.RESOURCE_UPDATE);
        KtMetaFile meta = metaFileStorageBindService.bind(req.getResourceId(), req.getStorageId(), req.getObjectKey(), req.getName());
        MaterialMetaFileVO vo = toMetaFileVo(meta);
        KtResource latest = findById(req.getResourceId()).orElse(resource);
        materialSearchSyncTrigger.publishFullDocument(latest);
        materialResourceTaskService.onUploadFileBound(req.getResourceId(), null);
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
                .collect(Collectors.toCollection(ArrayList::new));
        enrichSrcUrls(matched);
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

    /**
     * srcUrl/coverUrl 依赖 kt_meta_file 与存储 endpoint/bucket；无元数据或存储不可用时可能为 null。
     * previewUrl 对非文件夹资源始终返回应用内预览路径；视频另返回 keyframe 接口相对路径（未产出时访问可能 404）。
     */
    private void enrichSrcUrls(List<MaterialResourceVO> vos) {
        if (CollUtil.isEmpty(vos)) {
            return;
        }
        List<String> ids = vos.stream().map(MaterialResourceVO::getId).filter(StringUtils::hasText).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        List<KtMetaFile> metas = metaFileService.listByResourceIds(ids);
        Map<String, KtMetaFile> byResource = metas.stream()
                .collect(Collectors.toMap(KtMetaFile::getResourceId, Function.identity(), (a, b) -> a));
        Set<String> storageIds = metas.stream().map(KtMetaFile::getStorageId).filter(StringUtils::hasText).collect(Collectors.toSet());
        Map<String, KtFileStorage> storageById = new HashMap<>();
        for (String sid : storageIds) {
            KtFileStorage st = ktFileStorageMapper.selectById(sid);
            if (st != null) {
                storageById.put(sid, st);
            }
        }
        Set<String> videoIds = new LinkedHashSet<>();
        for (MaterialResourceVO v : vos) {
            if (v.getType() != null && ResourceTypeEnum.VIDEO.getType().equals(v.getType()) && StringUtils.hasText(v.getId())) {
                videoIds.add(v.getId());
            }
        }
        Map<String, String> coverUrlByResourceId = new HashMap<>();
        if (!videoIds.isEmpty()) {
            for (KtResourceDerivative c : resourceDerivativeService.listByResourceIdsAndDestinationType(
                    videoIds, ResourceDestinationTypes.COVER)) {
                String u = buildDerivativePublicUrl(c, storageById);
                if (StringUtils.hasText(u) && StringUtils.hasText(c.getResourceId())
                        && !coverUrlByResourceId.containsKey(c.getResourceId())) {
                    coverUrlByResourceId.put(c.getResourceId(), u);
                }
            }
        }
        for (MaterialResourceVO vo : vos) {
            vo.setSrcUrl(null);
            vo.setPreviewUrl(null);
            vo.setCoverUrl(null);
            vo.setKeyframeUrl(null);
            if (vo.getType() != null && ResourceTypeEnum.isFolder(vo.getType())) {
                continue;
            }
            KtMetaFile m = byResource.get(vo.getId());
            if (m != null) {
                KtFileStorage st = storageById.get(m.getStorageId());
                vo.setSrcUrl(materialStoragePublicUrlBuilder.build(st, m.getObjectKey()));
            }
            vo.setPreviewUrl(materialResourcePreviewLinkBuilder.buildRelativePreviewPath(vo.getId()));
            String cover = coverUrlByResourceId.get(vo.getId());
            applyCoverAndKeyframeUrls(vo, cover);
        }
    }

    /**
     * 封面：图片使用原图直链；视频：优先已登记的 COVER 衍生物 URL，否则仍返回关键帧接口相对路径（未就绪可能 404）。
     */
    private void applyCoverAndKeyframeUrls(MaterialResourceVO vo, String coverDerivativeUrlOrNull) {
        Integer t = vo.getType();
        if (t != null && ResourceTypeEnum.IMAGE.getType().equals(t) && StringUtils.hasText(vo.getSrcUrl())) {
            vo.setCoverUrl(vo.getSrcUrl());
        }
        if (t != null && ResourceTypeEnum.VIDEO.getType().equals(t)) {
            if (StringUtils.hasText(coverDerivativeUrlOrNull)) {
                vo.setCoverUrl(coverDerivativeUrlOrNull);
            }
            vo.setKeyframeUrl(materialResourcePreviewLinkBuilder.buildRelativeKeyframePath(vo.getId()));
        }
    }

    @Override
    public String resolvePreviewRedirectUrl(String resourceId) {
        if (!StringUtils.hasText(resourceId)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String rid = resourceId.trim();
        KtResource resource = findById(rid).orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), CatalogPermission.RESOURCE_LIST_VIEW);
        KtMetaFile meta = metaFileService.findByResourceId(rid)
                .orElseThrow(() -> BizException.of(ResultStatus.MATERIAL_FILE_NOT_FOUND));
        KtFileStorage st = ktFileStorageMapper.selectById(meta.getStorageId());
        String url = materialStoragePublicUrlBuilder.build(st, meta.getObjectKey());
        if (!StringUtils.hasText(url)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        return url;
    }

    @Override
    public Optional<String> resolveKeyframeRedirectUrl(String resourceId) {
        if (!StringUtils.hasText(resourceId)) {
            return Optional.empty();
        }
        String rid = resourceId.trim();
        KtResource resource = findById(rid).orElse(null);
        if (resource == null || !ResourceTypeEnum.VIDEO.getType().equals(resource.getType())) {
            return Optional.empty();
        }
        catalogService.requireOnCatalog(resource.getCatalogId(), CatalogPermission.RESOURCE_LIST_VIEW);
        return resourceDerivativeService.findByResourceAndType(rid, ResourceDestinationTypes.COVER)
                .map(d -> {
                    if (StringUtils.hasText(d.getExternalUrl())) {
                        return d.getExternalUrl().trim();
                    }
                    if (StringUtils.hasText(d.getStorageId()) && StringUtils.hasText(d.getObjectKey())) {
                        KtFileStorage st = ktFileStorageMapper.selectById(d.getStorageId());
                        return materialStoragePublicUrlBuilder.build(st, d.getObjectKey());
                    }
                    return null;
                })
                .filter(StringUtils::hasText);
    }

    @Override
    public MaterialDownloadUrlVO resolveDownloadUrl(String resourceId, String destinationType) {
        if (!StringUtils.hasText(resourceId) || !StringUtils.hasText(destinationType)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String rid = resourceId.trim();
        String norm = normalizeDestinationType(destinationType);
        KtResource resource = findById(rid).orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(resource.getCatalogId(), CatalogPermission.RESOURCE_LIST_VIEW);

        MaterialDownloadUrlVO vo = new MaterialDownloadUrlVO();
        vo.setResourceId(rid);
        vo.setDestinationType(norm);
        vo.setExpiresInSec(3600);
        if (ResourceDestinationTypes.SOURCE.equals(norm)) {
            String url = buildSourcePublicUrl(resource);
            if (!StringUtils.hasText(url)) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            vo.setActualDestinationType(ResourceDestinationTypes.SOURCE);
            vo.setUrl(url);
            return vo;
        }
        Optional<KtResourceDerivative> der = resourceDerivativeService.findByResourceAndType(rid, norm);
        if (der.isPresent()) {
            String url = buildDerivativePublicUrl(der.get(), null);
            if (StringUtils.hasText(url)) {
                vo.setActualDestinationType(norm);
                vo.setUrl(url);
                return vo;
            }
        }
        if (ResourceDestinationTypes.COVER.equals(norm) || ResourceDestinationTypes.SPRITE.equals(norm)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String sourceUrl = buildSourcePublicUrl(resource);
        if (!StringUtils.hasText(sourceUrl)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        vo.setActualDestinationType(ResourceDestinationTypes.SOURCE);
        vo.setUrl(sourceUrl);
        return vo;
    }

    @Override
    public void reportDownload(MaterialDownloadReportItemDTO body) {
        if (body == null || !StringUtils.hasText(body.getResourceId()) || !StringUtils.hasText(body.getDestinationType())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String rid = body.getResourceId().trim();
        KtResource r = findById(rid).orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        catalogService.requireOnCatalog(r.getCatalogId(), CatalogPermission.RESOURCE_LIST_VIEW);
        String title = StringUtils.hasText(body.getResourceTitle()) ? body.getResourceTitle() : r.getTitle();
        String op = userContextGateway.currentUserId();
        if (!StringUtils.hasText(op)) {
            op = "-";
        }
        String actual = StringUtils.hasText(body.getActualDestinationType()) ? body.getActualDestinationType().trim() : null;
        materialDataEventClient.tryReportDownload(
                op, rid, title, body.getDestinationType().trim(), actual);
    }

    @Override
    public void reportDownloadBatch(MaterialDownloadReportBatchDTO body) {
        if (body == null || body.getItems() == null) {
            return;
        }
        for (MaterialDownloadReportItemDTO item : body.getItems()) {
            if (item == null) {
                continue;
            }
            reportDownload(item);
        }
    }

    private MaterialResourceDerivativeVO toDerivativeVo(KtResourceDerivative d) {
        MaterialResourceDerivativeVO v = new MaterialResourceDerivativeVO();
        v.setDestinationType(d.getDestinationType());
        String url = buildDerivativePublicUrl(d, null);
        v.setAccessUrl(url);
        v.setAvailable(StringUtils.hasText(url));
        v.setFileSize(d.getFileSize());
        return v;
    }

    private String buildSourcePublicUrl(KtResource resource) {
        if (resource == null) {
            return null;
        }
        return metaFileService.findByResourceId(resource.getId())
                .map(meta -> {
                    KtFileStorage st = ktFileStorageMapper.selectById(meta.getStorageId());
                    return materialStoragePublicUrlBuilder.build(st, meta.getObjectKey());
                })
                .orElse(null);
    }

    /**
     * @param storageCache 可选；列表场景传入 meta 已加载的 storage 映射以便复用
     */
    private String buildDerivativePublicUrl(KtResourceDerivative d, Map<String, KtFileStorage> storageCache) {
        if (d == null) {
            return null;
        }
        if (StringUtils.hasText(d.getExternalUrl())) {
            return d.getExternalUrl().trim();
        }
        if (!StringUtils.hasText(d.getStorageId()) || !StringUtils.hasText(d.getObjectKey())) {
            return null;
        }
        KtFileStorage st = null;
        if (storageCache != null) {
            st = storageCache.get(d.getStorageId());
        }
        if (st == null) {
            st = ktFileStorageMapper.selectById(d.getStorageId());
            if (storageCache != null && st != null) {
                storageCache.put(d.getStorageId(), st);
            }
        }
        return materialStoragePublicUrlBuilder.build(st, d.getObjectKey());
    }

    private static String normalizeDestinationType(String raw) {
        if (raw == null) {
            return "";
        }
        String t = raw.trim();
        if (ResourceDestinationTypes.SOURCE.equalsIgnoreCase(t)) {
            return ResourceDestinationTypes.SOURCE;
        }
        if (ResourceDestinationTypes.COVER.equalsIgnoreCase(t)) {
            return ResourceDestinationTypes.COVER;
        }
        if (ResourceDestinationTypes.SPRITE.equalsIgnoreCase(t)) {
            return ResourceDestinationTypes.SPRITE;
        }
        return t;
    }

    private MaterialResourceVO enrichSingle(MaterialResourceVO vo) {
        if (vo == null) {
            return null;
        }
        enrichSrcUrls(List.of(vo));
        return vo;
    }

    private List<MaterialResourceVO> listDb(MaterialResourceListQueryDTO query, String normalizedCatalogId) {
        return findAll().stream()
                .filter(resource -> query == null || !StringUtils.hasText(normalizedCatalogId) || normalizedCatalogId.equals(resource.getCatalogId()))
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

    private String normalizeCatalogId(String catalogId) {
        return catalogService.normalizeResourceCatalogId(catalogId);
    }

    private String normalizeCatalogIdNullable(String catalogId) {
        if (!StringUtils.hasText(catalogId)) {
            return null;
        }
        return normalizeCatalogId(catalogId);
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
