package icu.jiapeng.qk.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.Instant;

@MongoEntity(collection = "ak_sk_qk")
public class AccessKey extends PanacheMongoEntity {

    @BsonProperty("ak")
    public String ak;

    @BsonProperty("sk")
    public String sk;

    @BsonProperty("remark")
    public String remark;

    @BsonProperty("enabled")
    public boolean enabled;

    @BsonProperty("createTime")
    public Instant createTime;

    @BsonProperty("updateTime")
    public Instant updateTime;
}
