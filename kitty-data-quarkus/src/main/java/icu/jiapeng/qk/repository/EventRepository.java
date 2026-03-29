package icu.jiapeng.qk.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import icu.jiapeng.qk.model.Event;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class EventRepository implements PanacheMongoRepository<Event> {

    @Inject
    MongoClient mongoClient;

    private MongoDatabase getDatabase() {
        return mongoClient.getDatabase("kitty_data");
    }

    private MongoCollection<Document> getCollection() {
        return getDatabase().getCollection("events");
    }

    public List<Document> aggregateStatsByEventType(Instant start, Instant end, List<String> eventTypes) {
        List<org.bson.conversions.Bson> pipeline = Arrays.asList(
                Aggregates.match(new Document("eventType", new Document("$in", eventTypes))
                        .append("timestamp", new Document("$gte", start).append("$lte", end))),
                Aggregates.group("$eventType",
                        Arrays.asList(
                                new BsonField("count", new Document("$sum", 1)),
                                new BsonField("sumValue", new Document("$sum", "$value"))
                        ))
        );

        return getCollection().aggregate(pipeline).into(new ArrayList<>());
    }

    public List<Document> aggregateStatsByOperator(Instant start, Instant end, List<String> eventTypes) {
        List<org.bson.conversions.Bson> pipeline = Arrays.asList(
                Aggregates.match(new Document("eventType", new Document("$in", eventTypes))
                        .append("timestamp", new Document("$gte", start).append("$lte", end))),
                Aggregates.group("$operator",
                        Arrays.asList(
                                new BsonField("count", new Document("$sum", 1)),
                                new BsonField("sumValue", new Document("$sum", "$value"))
                        ))
        );

        return getCollection().aggregate(pipeline).into(new ArrayList<>());
    }

    public List<Document> aggregateTrend(Instant start, Instant end, List<String> eventTypes, String groupBy) {
        Document groupId;
        switch (groupBy) {
            case "year":
                groupId = new Document("$year", "$timestamp");
                break;
            case "month":
                groupId = new Document("$month", "$timestamp");
                break;
            case "day":
                groupId = new Document("$dayOfMonth", "$timestamp");
                break;
            case "hour":
                groupId = new Document("$hour", "$timestamp");
                break;
            default:
                groupId = new Document("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$timestamp"));
        }

        List<org.bson.conversions.Bson> pipeline = Arrays.asList(
                Aggregates.match(new Document("eventType", new Document("$in", eventTypes))
                        .append("timestamp", new Document("$gte", start).append("$lte", end))),
                Aggregates.group(groupId,
                        Arrays.asList(
                                new BsonField("count", new Document("$sum", 1)),
                                new BsonField("sumValue", new Document("$sum", "$value"))
                        )),
                Aggregates.sort(new Document("_id", 1))
        );

        return getCollection().aggregate(pipeline).into(new ArrayList<>());
    }
}
