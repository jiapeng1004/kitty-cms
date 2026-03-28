package icu.jiapeng.kitty.material.embedding.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.jiapeng.kitty.material.searchsync.MaterialSearchIndexPort;
import icu.jiapeng.kitty.material.searchsync.service.MaterialSearchDocumentBuilder;
import icu.jiapeng.kitty.material.embedding.KtEmbeddingDTO;
import icu.jiapeng.kitty.material.embedding.MaterialVectorSourceKey;
import icu.jiapeng.kitty.material.search.MaterialSearchConstants;
import icu.jiapeng.kitty.material.embedding.entity.KtResourceEmbedding;
import icu.jiapeng.kitty.material.embedding.mapper.KtResourceEmbeddingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * 向量写入 MySQL（按资源+来源唯一）并 PATCH ES 文档中的 dense_vector 与来源标记。
 * <p>
 * 向量维度须与 {@link MaterialSearchConstants#VECTOR_DIMS}（含 NONE 占位）一致，否则 ES 写入会失败。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialVectorPersistenceService {

    private final KtResourceEmbeddingMapper resourceVectorMapper;
    private final MaterialSearchIndexPort materialSearchIndexPort;
    private final MaterialSearchDocumentBuilder documentBuilder;

    /**
     * 持久化向量并更新检索索引中的向量字段（索引中尚无主文档时仅写库、ES 跳过并打 warn）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void persistEmbedding(String resourceId, MaterialVectorSourceKey sourceKey, KtEmbeddingDTO embedding) {
        if (!StringUtils.hasText(resourceId) || sourceKey == null || embedding == null) {
            return;
        }
        String json = JSON.toJSONString(embedding.values());
        Date now = new Date();
        KtResourceEmbedding row = resourceVectorMapper.selectOne(new LambdaQueryWrapper<KtResourceEmbedding>()
                .eq(KtResourceEmbedding::getResourceId, resourceId)
                .eq(KtResourceEmbedding::getSourceType, sourceKey.code()));
        if (row == null) {
            KtResourceEmbedding ins = new KtResourceEmbedding();
            ins.setId(UUID.randomUUID().toString());
            ins.setResourceId(resourceId);
            ins.setSourceType(sourceKey.code());
            ins.setVectorJson(json);
            ins.setCreateTime(now);
            ins.setUpdateTime(now);
            resourceVectorMapper.insert(ins);
        } else {
            row.setVectorJson(json);
            row.setUpdateTime(now);
            resourceVectorMapper.updateById(row);
        }
        indexEmbedding(resourceId, sourceKey, embedding);
    }

    private void indexEmbedding(String resourceId, MaterialVectorSourceKey sourceKey, KtEmbeddingDTO embedding) {
        Map<String, Object> doc = documentBuilder.buildFullDocument(resourceId);
        if (doc == null) {
            // 文档缺失通常意味着资源尚未完成编目快照；这里直接跳过，由调用方按链路补齐。
            log.warn("material vector ES index skipped (resource not found), resourceId={} source={}", resourceId, sourceKey.code());
            return;
        }
        doc.put(MaterialSearchConstants.VECTOR_FIELD_NAME, embedding.values());
        doc.put(MaterialSearchConstants.EMBEDDING_SOURCE_FIELD_NAME, sourceKey.code());
        materialSearchIndexPort.indexOrReplace(resourceId, doc);
    }
}
