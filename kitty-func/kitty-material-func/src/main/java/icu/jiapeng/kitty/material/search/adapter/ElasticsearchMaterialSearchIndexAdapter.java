package icu.jiapeng.kitty.material.search.adapter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorSimilarity;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.json.JsonData;
import icu.jiapeng.kitty.material.searchsync.MaterialSearchIndexPort;
import icu.jiapeng.kitty.material.search.MaterialSearchConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * Elasticsearch 8 Java API 实现。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ElasticsearchMaterialSearchIndexAdapter implements MaterialSearchIndexPort {

    private final ElasticsearchClient client;

    @Override
    public void indexOrReplace(String resourceId, Map<String, Object> document) {
        try {
            ensureIndex();
            client.index(IndexRequest.of(i -> i.index(MaterialSearchConstants.INDEX_NAME).id(resourceId).document(document)));
            log.debug("material ES index upsert id={}", resourceId);
        } catch (IOException e) {
            throw new IllegalStateException("material ES index failed: " + resourceId, e);
        }
    }

    @Override
    public void patchDocument(String resourceId, Map<String, Object> partialDocument) {
        JsonData doc = JsonData.of(partialDocument);
        try {
            ensureIndex();
            client.update(
                    UpdateRequest.of(u -> u.index(MaterialSearchConstants.INDEX_NAME).id(resourceId).doc(doc)),
                    JsonData.class);
            log.debug("material ES partial update id={}", resourceId);
        } catch (Exception e) {
            throw new IllegalStateException("material ES patch failed: " + resourceId, e);
        }
    }

    @Override
    public void removeDocument(String resourceId) {
        if (resourceId == null || resourceId.isBlank()) {
            return;
        }
        try {
            ensureIndex();
            DeleteResponse r = client.delete(DeleteRequest.of(d -> d.index(MaterialSearchConstants.INDEX_NAME).id(resourceId)));
            if (r.result() == Result.NotFound) {
                log.debug("material ES delete skipped (not found) id={}", resourceId);
                return;
            }
            log.debug("material ES document removed id={}", resourceId);
        } catch (IOException e) {
            throw new IllegalStateException("material ES delete failed: " + resourceId, e);
        }
    }

    private void ensureIndex() throws IOException {
        if (!client.indices().exists(e -> e.index(MaterialSearchConstants.INDEX_NAME)).value()) {
            String vf = MaterialSearchConstants.VECTOR_FIELD_NAME;
            String src = MaterialSearchConstants.EMBEDDING_SOURCE_FIELD_NAME;
            int dims = MaterialSearchConstants.VECTOR_DIMS;
            DenseVectorSimilarity sim = normalizeVectorSimilarity(MaterialSearchConstants.VECTOR_SIMILARITY);
            client.indices().create(c -> c
                    .index(MaterialSearchConstants.INDEX_NAME)
                    .mappings(m -> m
                            .properties(vf, Property.of(p -> p.denseVector(DenseVectorProperty.of(d -> d
                                    .dims(dims)
                                    .index(true)
                                    .similarity(sim)))))
                            .properties(src, Property.of(p -> p.keyword(k -> k)))));
            log.info("material ES index created with dense_vector field={} dims={}: {}", vf, dims, MaterialSearchConstants.INDEX_NAME);
        }
    }

    /** ES dense_vector similarity：cosine / dot_product / l2_norm / max_inner_product */
    private static DenseVectorSimilarity normalizeVectorSimilarity(String raw) {
        if (raw == null || raw.isBlank()) {
            return DenseVectorSimilarity.Cosine;
        }
        return switch (raw.trim().toLowerCase()) {
            case "cosine" -> DenseVectorSimilarity.Cosine;
            case "dot_product" -> DenseVectorSimilarity.DotProduct;
            case "l2_norm" -> DenseVectorSimilarity.L2Norm;
            case "max_inner_product", "maxinnerproduct" -> DenseVectorSimilarity.MaxInnerProduct;
            default -> DenseVectorSimilarity.Cosine;
        };
    }
}
