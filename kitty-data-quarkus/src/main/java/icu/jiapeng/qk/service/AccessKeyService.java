package icu.jiapeng.qk.service;

import icu.jiapeng.qk.repository.AccessKeyRepository;
import icu.jiapeng.qk.model.AccessKey;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

@ApplicationScoped
public class AccessKeyService {

    private static final Logger LOG = Logger.getLogger(AccessKeyService.class);

    @Inject
    AccessKeyRepository accessKeyRepository;

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    public void refresh() {
        LOG.info("Refreshing AK/SK cache...");
        try {
            List<AccessKey> keys = accessKeyRepository.findAllEnabled();
            Map<String, String> newCache = new ConcurrentHashMap<>();
            for (AccessKey key : keys) {
                newCache.put(key.ak, key.sk);
            }
            cache.clear();
            cache.putAll(newCache);
            initialized = true;
            LOG.infof("AK/SK cache refreshed, %d keys loaded", keys.size());
        } catch (Exception e) {
            LOG.error("Failed to refresh AK/SK cache", e);
        }
    }

    public String getSk(String ak) {
        return cache.get(ak);
    }

    public boolean isInitialized() {
        return initialized;
    }
}
