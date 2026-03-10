package de.fhdw.vendix.store.application.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class RegisterHealthCheckSchedule {

    // TODO: might be redundant. should be implemented with an health actuator
//    private static final Logger log = LoggerFactory.getLogger(RegisterHealthCheckSchedule.class);
//
//    @Scheduled(cron = "0 */1 * * * *", zone = "Europe/Berlin")
//    public void performPingChecks() {
//        log.atInfo().log("[SCHEDULED] Performing ping checks...");
//
//        registry.findAllActiveRegistries().forEach(instance -> pingInstance(instance)
//                .doOnSuccess(v -> {
//                    instance.setOnline(true);
//                    instance.setLastSeen(Instant.now());
//                })
//                .onErrorResume(ex -> {
//                    instance.setOnline(false);
//                    log.atWarn().log("Kassensystem offline: {}; Reason: {}", instance.getSystemClientDTO().getId(), ex.getMessage());
//                    return Mono.empty();
//                })
//                .subscribe());
//
//        log.atInfo().log("[SCHEDULED] Successfully performed ping checks");
//    }
//
//    private Mono<Void> pingInstance(RegisterClient instance) {
//        return webClient.get()
//                .uri("http://%s:%d/actuator/health".formatted(instance.getSystemClientDTO().getHost(), instance.getSystemClientDTO().getPort()))
//                .retrieve()
//                .bodyToMono(Void.class)
//                .timeout(Duration.ofSeconds(3));
//    }
}