package de.fhdw.vendix.pos.utility.initializer;

import de.fhdw.vendix.commons.core.api.dto.RegisterDTO;
import de.fhdw.vendix.commons.core.api.dto.StoreDTO;
import de.fhdw.vendix.commons.core.api.dto.SystemClientDTO;
import de.fhdw.vendix.pos.utility.RegisterClient;
import de.fhdw.vendix.pos.utility.StoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Order(1)
@Component
    public class FilialConnectorInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FilialConnectorInitializer.class);

    private final String applicationName;
    private final String activeProfile;
    private final boolean connectToFilialOnStartup;

    private final StoreClient storeClient;
    private final RegisterClient registerClient;

    public FilialConnectorInitializer(
            @Value("${spring.application.name:fallback}") String applicationName,
            @Value("${spring.profiles.active:fallback}") String activeProfile,
            @Value("${spring.kassensystem.connect-to-filial:true}") boolean connectToFilialOnStartup,
            StoreClient storeClient, RegisterClient registerClient) {
        this.applicationName = applicationName;
        this.activeProfile = activeProfile;
        this.connectToFilialOnStartup = connectToFilialOnStartup;
        this.storeClient = storeClient;
        this.registerClient = registerClient;
    }

    private static final int MAX_RETRIES = 10;

    @Override
    public void run(String... args) throws Exception {
        log.atInfo().log("spring.kassensystem.connect-to-filial-on-startup is: {}", connectToFilialOnStartup);

        if (!connectToFilialOnStartup) {
            return;
        }

        SystemClientDTO dto = new SystemClientDTO(
                "%s-%s-%s".formatted(applicationName, activeProfile, UUID.randomUUID()),
                "localhost",
                registerClient.getPort()
        );

        log.atInfo().log("Suche nach Filialsystem...");

        int attempt = 0;
        long delayMs = 2000;

        while (attempt < MAX_RETRIES) {
            try {
                registerAtFilialsystem(dto);
                log.atInfo().log("Filialsystem gefunden. Kassensystem erfolgreich registriert.");
                return;
            } catch (Exception e) {
                attempt++;
                log.atWarn().log("Filialsystem nicht erreichbar (Versuch {}/{}). Nächster Versuch in {} ms...",
                        attempt, MAX_RETRIES, delayMs);
                if (attempt >= MAX_RETRIES) {
                    throw new IllegalStateException(
                            "Filialsystem nach " + MAX_RETRIES + " Versuchen nicht erreichbar. Anwendung wird beendet.", e);
                }
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Startvorgang unterbrochen.", ie);
                }
                delayMs = Math.min(delayMs * 2, 30000); // exponentieller Backoff, max 30 Sek.
            }
        }
    }

    private void registerAtFilialsystem(SystemClientDTO dto) {
        ResponseEntity<RegisterDTO> registerDTO = storeClient.getWebClient()
                .post()
                .uri("api/registry")
                .bodyValue(dto)
                .retrieve()
                .toEntity(RegisterDTO.class)
                .block();
        if (registerDTO != null && registerDTO.getStatusCode() == HttpStatus.OK && registerDTO.getBody() != null) {
            storeClient.setStoreDTO(new StoreDTO(registerDTO.getBody().getStore()));
            registerClient.setRegister(registerDTO.getBody());
        }
    }
}