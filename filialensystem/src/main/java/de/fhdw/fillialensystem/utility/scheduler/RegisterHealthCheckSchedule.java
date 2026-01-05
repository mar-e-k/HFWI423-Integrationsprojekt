package de.fhdw.fillialensystem.utility.scheduler;

import de.fhdw.fillialensystem.utility.RegisterClient;
import de.fhdw.fillialensystem.persistence.service.other.RegisterRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
public class RegisterHealthCheckSchedule {

    private static final Logger log = LoggerFactory.getLogger(RegisterHealthCheckSchedule.class);

    private final RegisterRegistryService registry;
    private final WebClient webClient;

    public RegisterHealthCheckSchedule(RegisterRegistryService registry) {
        this.registry = registry;
        this.webClient = WebClient.builder().build();
    }

    @Scheduled(cron = "0 */1 * * * *", zone = "Europe/Berlin")
    public void performPingChecks() {
        log.atInfo().log("[SCHEDULED] Performing ping checks...");

        registry.findAllActiveRegistries().forEach(instance -> pingInstance(instance)
                .doOnSuccess(v -> {
                    instance.setOnline(true);
                    instance.setLastSeen(Instant.now());
                })
                .onErrorResume(ex -> {
                    instance.setOnline(false);
                    log.atWarn().log("Kassensystem offline: {}; Reason: {}", instance.getSystemClientDTO().getId(), ex.getMessage());
                    return Mono.empty();
                })
                .subscribe());

        log.atInfo().log("[SCHEDULED] Successfully performed ping checks");
    }

    private Mono<Void> pingInstance(RegisterClient instance) {
        return webClient.get()
                .uri("http://%s:%d/actuator/health".formatted(instance.getSystemClientDTO().getHost(), instance.getSystemClientDTO().getPort()))
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(3));
    }
}