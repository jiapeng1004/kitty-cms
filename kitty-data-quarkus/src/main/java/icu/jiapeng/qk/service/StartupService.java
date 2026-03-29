package icu.jiapeng.qk.service;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class StartupService {

    private static final Logger LOG = Logger.getLogger(StartupService.class);

    @Inject
    AccessKeyService accessKeyService;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("Application starting, triggering initial AK/SK cache load...");
        accessKeyService.refresh();
    }
}
