package icu.jiapeng.kitty.clickqk.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.InsertOneResult;
import icu.jiapeng.kitty.clickqk.constants.Constant;
import icu.jiapeng.kitty.clickqk.constants.Converter;
import icu.jiapeng.kitty.clickqk.dto.ReportReq;
import icu.jiapeng.kitty.clickqk.entity.Operate;
import icu.jiapeng.kitty.clickqk.service.ReportService;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoCollection;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.list.ReactiveListCommands;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Objects;
import java.util.UUID;

/**
 *
 *
 * @author jiapeng
 * @since 2026/3/21
 */
@ApplicationScoped
public class MongoReportServiceImpl implements ReportService {

    @Inject
    ReactiveMongoClient reactiveMongoClient;

    @Inject
    ReactiveRedisDataSource reactiveRedisDataSource;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Uni<Void> report(String ak, Long timestamp, ReportReq reportReq) {
        ReactiveMongoCollection<Operate> operateCollection = reactiveMongoClient.getDatabase(Constant.DB).getCollection(Operate.class.getSimpleName(), Operate.class);
        Operate operate = Converter.INSTANCE.toOperate(reportReq);
        operate.setTimestamp(timestamp);
        if (Objects.isNull(ak)) {
            return Uni.createFrom().failure(new RuntimeException("ak is null"));
        }
        operate.setAk(ak);
        Uni<InsertOneResult> insertOneResultUni = operateCollection.insertOne(operate);
        return insertOneResultUni.replaceWithVoid();
    }

    @Override
    public Uni<Void> reportAsync(String ak, Long timestamp, ReportReq reportReq) {
        if (ak == null || ak.isBlank()) {
            return Uni.createFrom().failure(new IllegalArgumentException("ak is blank"));
        }
        Operate operate = Converter.INSTANCE.toOperate(reportReq);
        operate.setTimestamp(timestamp);
        operate.setAk(ak);
        if (operate.getId() == null) {
            operate.setId(UUID.randomUUID().toString());
        }
        final String json;
        try {
            json = objectMapper.writeValueAsString(operate);
        } catch (JsonProcessingException e) {
            return Uni.createFrom().failure(e);
        }
        ReactiveListCommands<String, String> list = reactiveRedisDataSource.list(String.class);
        return list.lpush(Constant.REDIS_REPORT_QUEUE, json).replaceWithVoid();
    }
}
