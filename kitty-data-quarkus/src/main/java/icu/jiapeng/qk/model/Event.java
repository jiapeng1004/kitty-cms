package icu.jiapeng.qk.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.Instant;

@MongoEntity(collection = "events_qk")
public class Event extends PanacheMongoEntity {

    @BsonProperty("eventType")
    public String eventType;

    @BsonProperty("operator")
    public String operator;

    @BsonProperty("value")
    public long value;

    @BsonProperty("detailJson")
    public String detailJson;

    @BsonProperty("timestamp")
    public Instant timestamp;

    @BsonProperty("createTime")
    public Instant createTime;
}
