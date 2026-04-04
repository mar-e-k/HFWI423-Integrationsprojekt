package de.fhdw.vendix.orchestrator;

import de.fhdw.vendix.commons.spring.security.context.app.AppContext;

import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.LockProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class OrchestratorApplication {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorApplication.class);

    private final AppContext appContext;
    private final LockProxyService lockProxyService;

    public OrchestratorApplication(AppContext appContext, LockProxyService lockProxyService) {
        this.appContext = appContext;
        this.lockProxyService = lockProxyService;
    }

    public static void main(String[] args) {
        SpringApplication.run(OrchestratorApplication.class, args);
    }

    @EventListener
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.atInfo().log(appContext.getInstanceUUID().toString());
        log.atInfo().log(appContext.getServerName());
        log.atInfo().log(appContext.getHostName());
        log.atInfo().log(String.valueOf(appContext.getServerPort()));
        log.atInfo().log("Application running at http://{}:{}/", appContext.getServerName(), appContext.getServerPort());

        lockProxyService.getLocks(null, null, null);
    }
}