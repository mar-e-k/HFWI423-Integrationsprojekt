package de.fhdw.vendix.pos;

import de.fhdw.vendix.commons.spring.app.context.system.SystemContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class PosApplication {

    private static final Logger log = LoggerFactory.getLogger(PosApplication.class);

    private final SystemContext systemContext;

    public PosApplication(SystemContext systemContext) {
        this.systemContext = systemContext;
    }

    static void main(String[] args) {
        SpringApplication.run(PosApplication.class, args);
    }

    @EventListener
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.atInfo().log("Application running at http://{}:{}/",
                systemContext.getServerName(),
                systemContext.getServerPort()
        );
    }
}