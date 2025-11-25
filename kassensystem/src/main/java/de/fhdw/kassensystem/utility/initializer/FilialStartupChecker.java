package de.fhdw.kassensystem.utility.initializer;

import de.fhdw.commons.api.dto.SystemClientDTO;
import de.fhdw.kassensystem.utility.FilialClient;
import de.fhdw.kassensystem.utility.ServerPortProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Order(1)
@Component
    public class FilialStartupChecker implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FilialStartupChecker.class);

    private final String applicationName;
    private final String activeProfile;
    private final boolean connectToFilialOnStartup;

    private final FilialClient filialClient;
    private final ServerPortProvider serverPortProvider;

    public FilialStartupChecker(
            @Value("${spring.application.name:fallback}") String applicationName,
            @Value("${spring.profiles.active:fallback}") String activeProfile,
            @Value("${spring.kassensystem.connect-to-filial-on-startup:true}") boolean connectToFilialOnStartup,
            FilialClient filialClient,
            ServerPortProvider serverPortProvider) {
        this.applicationName = applicationName;
        this.activeProfile = activeProfile;
        this.connectToFilialOnStartup = connectToFilialOnStartup;
        this.filialClient = filialClient;
        this.serverPortProvider = serverPortProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.atInfo().log("spring.kassensystem.connect-to-filial-on-startup: {}", connectToFilialOnStartup);

        if (!connectToFilialOnStartup) {
            return;
        }

        SystemClientDTO dto = new SystemClientDTO(
                "%s-%s-%s".formatted(applicationName, activeProfile, UUID.randomUUID()),
                "localhost",
                serverPortProvider.getPort()
        );

        log.atInfo().log("Searching for Filialsystem...");

        while (true) {
            try {
                registerAtFilialsystem(dto);
                break;
            } catch (Exception e) {
                log.atWarn().log("Filialsystem not available. Retrying in 5 seconds...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {}
            }
        }

        log.atInfo().log("Filialsystem found. Kassensystem registered successfully.");
    }

    private void registerAtFilialsystem(SystemClientDTO dto) {
        filialClient.getWebClient()
                .post()
                .uri("api/registry")
                .bodyValue(dto)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}