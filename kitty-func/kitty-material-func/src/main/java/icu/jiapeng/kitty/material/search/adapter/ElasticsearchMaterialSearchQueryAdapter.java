package icu.jiapeng.kitty.material.search.adapter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import icu.jiapeng.kitty.material.searchsync.MaterialSearchQueryPort;
import icu.jiapeng.kitty.material.search.MaterialSearchConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ES8：全文 multi_match + kNN（dense_vector）。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ElasticsearchMaterialSearchQueryAdapter implements MaterialSearchQueryPort {

    private final ElasticsearchClient client;

    @Override
    public List<String> searchIdsByFullText(String keyword, String catalogId, String parentId, int size) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        int n = Math.max(1, Math.min(size, 500));
        try {
            SearchResponse<Object> resp = client.search(s -> s
                            .index(MaterialSearchConstants.INDEX_NAME)
                            .size(n)
                            .query(q -> q.bool(b -> {
                                b.must(m -> m.multiMatch(mm -> mm
                                        .query(keyword)
                                        .fields("title^2")
                                        .type(TextQueryType.BestFields)
                                        .operator(Operator.Or)));
                                if (catalogId != null && !catalogId.isBlank()) {
                                    String c = catalogId.trim();
                                    b.filter(f -> f.matchPhrase(mp -> mp.field("catalogId").query(c)));
                                }
                                if (parentId != null && !parentId.isBlank()) {
                                    String p = parentId.trim();
                                    b.filter(f -> f.matchPhrase(mp -> mp.field("parentId").query(p)));
                                }
                                return b;
                            })),
                    Object.class);
            return hitsToIds(resp);
        } catch (IOException e) {
            throw new IllegalStateException("material ES fulltext search failed", e);
        }
    }

    @Override
    public List<String> searchIdsByKnn(float[] queryVector, String catalogId, String parentId, int k) {
        if (queryVector == null || queryVector.length == 0) {
            return List.of();
        }
        int kk = Math.max(1, Math.min(k, 500));
        int numCandidates = Math.min(10_000, Math.max(kk * 4, 100));
        List<Float> qv = toBoxed(queryVector);
        String vf = MaterialSearchConstants.VECTOR_FIELD_NAME;
        try {
            SearchResponse<Object> resp = client.search(s -> s
                            .index(MaterialSearchConstants.INDEX_NAME)
                            .size(kk)
                            .knn(kn -> kn
                                    .field(vf)
                                    .queryVector(qv)
                                    .k(kk)
                                    .numCandidates(numCandidates)
                                    .filter(scopeFilter(catalogId, parentId))),
                    Object.class);
            return hitsToIds(resp);
        } catch (IOException e) {
            throw new IllegalStateException("material ES knn search failed", e);
        }
    }

    private static Query scopeFilter(String catalogId, String parentId) {
        boolean hasC = catalogId != null && !catalogId.isBlank();
        boolean hasP = parentId != null && !parentId.isBlank();
        if (!hasC && !hasP) {
            return Query.of(q -> q.matchAll(m -> m));
        }
        return Query.of(q -> q.bool(b -> {
            if (hasC) {
                String c = Objects.requireNonNull(catalogId).trim();
                b.filter(f -> f.matchPhrase(mp -> mp.field("catalogId").query(c)));
            }
            if (hasP) {
                String p = Objects.requireNonNull(parentId).trim();
                b.filter(f -> f.matchPhrase(mp -> mp.field("parentId").query(p)));
            }
            return b;
        }));
    }

    private static List<Float> toBoxed(float[] v) {
        List<Float> list = new ArrayList<>(v.length);
        for (float x : v) {
            list.add(x);
        }
        return list;
    }

    private static List<String> hitsToIds(SearchResponse<Object> resp) {
        if (resp == null || resp.hits() == null || resp.hits().hits() == null) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (Hit<Object> hit : resp.hits().hits()) {
            String id = hit.id();
            if (id != null && !id.isBlank()) {
                out.add(id);
            }
        }
        return out;
    }
}
