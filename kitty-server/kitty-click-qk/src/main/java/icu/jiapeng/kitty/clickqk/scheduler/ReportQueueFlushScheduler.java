package icu.jiapeng.kitty.clickqk.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import icu.jiapeng.kitty.clickqk.constants.Constant;
import icu.jiapeng.kitty.clickqk.entity.Operate;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoCollection;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.list.ReactiveListCommands;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 将 Redis 队列中的异步上报批量写入 MongoDB。
 */
@Slf4j
@ApplicationScoped
public class ReportQueueFlushScheduler {

    @Inject
    ReactiveMongoClient reactiveMongoClient;

    @Inject
    ReactiveRedisDataSource reactiveRedisDataSource;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "kittyclick.report.flush.batch-size", defaultValue = "100")
    int batchSize;

    private ReactiveListCommands<String, String> list() {
        return reactiveRedisDataSource.list(String.class);
    }

    @Scheduled(every = "1s", concurrentExecution = ConcurrentExecution.SKIP)
    Uni<Void> flushReportQueueToMongo() {
        return popBatch(new ArrayList<>(), Math.max(1, batchSize))
                .onItem().transformToUni(jsonLines -> {
                    if (jsonLines.isEmpty()) {
                        return Uni.createFrom().voidItem();
                    }
                    List<Operate> batch = new ArrayList<>(jsonLines.size());
                    for (String json : jsonLines) {
                        try {
                            batch.add(objectMapper.readValue(json, Operate.class));
                        } catch (Exception e) {
                            log.warn("skip invalid report json from redis queue: {}", json, e);
                        }
                    }
                    if (batch.isEmpty()) {
                        return Uni.createFrom().voidItem();
                    }
                    ReactiveMongoCollection<Operate> col = reactiveMongoClient.getDatabase(Constant.DB)
                            .getCollection(Operate.class.getSimpleName(), Operate.class);
                    return col.insertMany(batch)
                            .onItem().invoke(r -> log.debug("flushed {} operates to mongodb", batch.size()))
                            .replaceWithVoid();
                });
    }

    /**
     * 从队列尾部 RPOP，直到凑满 batch 或队列空。
     */
    private Uni<List<String>> popBatch(List<String> acc, int max) {
        if (acc.size() >= max) {
            return Uni.createFrom().item(acc);
        }
        return list().rpop(Constant.REDIS_REPORT_QUEUE)
                .onItem().transformToUni(item -> {
                    if (item == null) {
                        return Uni.createFrom().item(acc);
                    }
                    acc.add(item);
                    return popBatch(acc, max);
                });
    }
}
