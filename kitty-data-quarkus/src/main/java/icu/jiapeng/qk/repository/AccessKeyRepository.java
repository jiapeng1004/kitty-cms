package icu.jiapeng.qk.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import icu.jiapeng.qk.model.AccessKey;

import java.util.List;

@ApplicationScoped
public class AccessKeyRepository implements PanacheMongoRepository<AccessKey> {

    public List<AccessKey> findAllEnabled() {
        return list("enabled", true);
    }
}
