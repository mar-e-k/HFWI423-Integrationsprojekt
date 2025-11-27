package de.fhdw.fillialensystem.api.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
public class KassensystemHealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(KassensystemHealthCheckService.class);

    private final KassensystemRegistryService registry;
    private final WebClient webClient;

    public KassensystemHealthCheckService(KassensystemRegistryService registry) {
        this.registry = registry;
        this.webClient = WebClient.builder().build();
    }

    @Scheduled(fixedDelay = 60000)
    public void performPingChecks() {
        log.atInfo().log("Performing ping checks...");
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
        log.atInfo().log("Successfully performed ping checks");
    }

    private Mono<Void> pingInstance(KassensystemInstance instance) {
        return webClient.get()
                .uri("http://%s:%d/actuator/health".formatted(instance.getSystemClientDTO().getHost(), instance.getSystemClientDTO().getPort()))
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(3));
    }
}