package icu.jiapeng.kitty.clickqk.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import icu.jiapeng.kitty.clickqk.constants.Constant;
import icu.jiapeng.kitty.clickqk.entity.AkSk;
import icu.jiapeng.kitty.clickqk.service.AkSkAuthService;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.keys.ReactiveKeyCommands;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

import static com.mongodb.client.model.Filters.eq;

@ApplicationScoped
public class MongoAkSkAuthService implements AkSkAuthService {

    @Inject
    ReactiveMongoClient reactiveMongoClient;

    @Inject
    ReactiveRedisDataSource reactiveRedisDataSource;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "kittyclick.cache.aksk-ttl-seconds", defaultValue = "3600")
    long akSkCacheTtlSeconds;

    private ReactiveValueCommands<String, String> value() {
        return reactiveRedisDataSource.value(String.class);
    }

    private ReactiveKeyCommands<String> key() {
        return reactiveRedisDataSource.key();
    }

    @Override
    public Uni<String> findSkByAk(String ak) {
        if (ak == null || ak.isBlank()) {
            return Uni.createFrom().nullItem();
        }
        String cacheKey = Constant.REDIS_CACHE_AKSK_BY_AK + ak;
        return value().get(cacheKey)
                .onItem().transformToUni(cached -> {
                    if (StrUtil.isNotBlank(cached)) {
                        return Uni.createFrom().item(cached);
                    }
                    return loadSkFromMongoByAk(ak)
                            .onItem().transformToUni(sk -> {
                                if (StrUtil.isBlank(sk)) {
                                    return Uni.createFrom().nullItem();
                                }
                                Duration ttl = Duration.ofSeconds(akSkCacheTtlSeconds);
                                return value().set(cacheKey, sk)
                                        .chain(() -> key().expire(cacheKey, ttl))
                                        .replaceWith(sk);
                            });
                });
    }

    @Override
    public Uni<AkSk> findById(String id) {
        if (id == null || id.isBlank()) {
            return Uni.createFrom().nullItem();
        }
        String cacheKey = Constant.REDIS_CACHE_AKSK_BY_ID + id;
        return value().get(cacheKey)
                .onItem().transformToUni(json -> {
                    if (StrUtil.isNotBlank(json)) {
                        try {
                            return Uni.createFrom().item(objectMapper.readValue(json, AkSk.class));
                        } catch (JsonProcessingException e) {
                            return Uni.createFrom().failure(e);
                        }
                    }
                    return loadAkSkFromMongoById(id)
                            .onItem().transformToUni(doc -> {
                                if (doc == null) {
                                    return Uni.createFrom().nullItem();
                                }
                                try {
                                    String payload = objectMapper.writeValueAsString(doc);
                                    Duration ttl = Duration.ofSeconds(akSkCacheTtlSeconds);
                                    return value().set(cacheKey, payload)
                                            .chain(() -> key().expire(cacheKey, ttl))
                                            .replaceWith(doc);
                                } catch (JsonProcessingException e) {
                                    return Uni.createFrom().failure(e);
                                }
                            });
                });
    }

    private Uni<String> loadSkFromMongoByAk(String ak) {
        return reactiveMongoClient.getDatabase(Constant.DB)
                .getCollection(AkSk.class.getSimpleName(), AkSk.class)
                .find(eq(AkSk.Fields.ak, ak))
                .collect().asList()
                .map(list -> {
                    if (list.isEmpty()) {
                        return null;
                    }
                    String sk = list.get(0).getSk();
                    return StrUtil.isBlank(sk) ? null : sk;
                });
    }

    private Uni<AkSk> loadAkSkFromMongoById(String id) {
        return reactiveMongoClient.getDatabase(Constant.DB)
                .getCollection(AkSk.class.getSimpleName(), AkSk.class)
                .find(eq("_id", id))
                .collect().asList()
                .map(list -> list.isEmpty() ? null : list.get(0));
    }
}
