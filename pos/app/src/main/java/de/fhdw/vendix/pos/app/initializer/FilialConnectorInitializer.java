package de.fhdw.vendix.pos.app.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

//@Order(1)
//@Component
//    public class FilialConnectorInitializer implements ApplicationRunner {
//
//    private static final Logger log = LoggerFactory.getLogger(FilialConnectorInitializer.class);
//
//    private final String applicationName;
//    private final String activeProfile;
//    private final boolean connectToFilialOnStartup;
//
//    private final StoreClient storeClient;
//    private final RegisterClient registerClient;
//
//    public FilialConnectorInitializer(
//            @Value("${spring.application.name:fallback}") String applicationName,
//            @Value("${spring.profiles.active:fallback}") String activeProfile,
//            @Value("${spring.kassensystem.connect-to-filial:true}") boolean connectToFilialOnStartup,
//            StoreClient storeClient, RegisterClient registerClient) {
//        this.applicationName = applicationName;
//        this.activeProfile = activeProfile;
//        this.connectToFilialOnStartup = connectToFilialOnStartup;
//        this.storeClient = storeClient;
//        this.registerClient = registerClient;
//    }
//
//    @Override
//    public void run(ApplicationArguments args) {
//        log.atInfo().log("spring.kassensystem.connect-to-filial-on-startup is: {}", connectToFilialOnStartup);
//
//        if (!connectToFilialOnStartup) {
//            return;
//        }
//
//        SystemClientDTO dto = new SystemClientDTO(
//                "%s-%s-%s".formatted(applicationName, activeProfile, UUID.randomUUID()),
//                "localhost",
//                registerClient.getPort()
//        );
//
//        log.atInfo().log("Searching for Filialsystem...");
//
//        while (true) {
//            try {
//                registerAtFilialsystem(dto);
//                break;
//            } catch (Exception e) {
//                log.atWarn().log("Filialsystem not available. Retrying in 5 seconds...");
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException ignored) {}
//            }
//        }
//
//        log.atInfo().log("Filialsystem found. Kassensystem registered successfully.");
//    }
//
//    private void registerAtFilialsystem(SystemClientDTO dto) {
//        ResponseEntity<RegisterDTO> registerDTO = storeClient.getWebClient()
//                .post()
//                .uri("api/registry")
//                .bodyValue(dto)
//                .retrieve()
//                .toEntity(RegisterDTO.class)
//                .block();
//        if (registerDTO != null && registerDTO.getStatusCode() == HttpStatus.OK && registerDTO.getBody() != null) {
//            storeClient.setStoreDTO(new StoreDTO(registerDTO.getBody().getStore()));
//            registerClient.setRegister(registerDTO.getBody());
//        }
//    }
//}