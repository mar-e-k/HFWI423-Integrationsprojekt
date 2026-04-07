package de.fhdw.vendix.orchestrator;

import de.fhdw.vendix.commons.spring.security.context.app.AppContext;

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

    public OrchestratorApplication(AppContext appContext) {
        this.appContext = appContext;
    }

    public static void main(String[] args) {
        SpringApplication.run(OrchestratorApplication.class, args);
    }

    @EventListener
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.atInfo().log("Application running at http://{}:{}/", appContext.getServerName(), appContext.getServerPort());
    }
}