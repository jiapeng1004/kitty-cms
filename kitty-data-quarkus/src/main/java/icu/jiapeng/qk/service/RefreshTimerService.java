package icu.jiapeng.qk.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class RefreshTimerService {

    private static final Logger LOG = Logger.getLogger(RefreshTimerService.class);

    @Inject
    AccessKeyService accessKeyService;


    @Scheduled(every = "{auth.refresh}")
    void refreshAkSk() {
        LOG.debug("Timer triggered AK/SK refresh");
        accessKeyService.refresh();
    }
}
