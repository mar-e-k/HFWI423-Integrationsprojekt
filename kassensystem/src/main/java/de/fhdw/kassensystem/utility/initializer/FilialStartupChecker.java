package de.fhdw.kassensystem.utility.initializer;

import de.fhdw.kassensystem.rest.FilialClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
public class FilialStartupChecker implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FilialStartupChecker.class);

    private final FilialClient client;

    public FilialStartupChecker(FilialClient client) {
        this.client = client;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.atInfo().log("Searching for Filialsystem...");
        while (true) {
            try {
                client.checkStatus().block();
                break;
            } catch (Exception e) {
                log.atInfo().log("Waiting for available Filialsystem...");
                Thread.sleep(5000);
            }
        }
        log.atInfo().log("Filialsystem found. Starting Kassensystem...");
    }
}
