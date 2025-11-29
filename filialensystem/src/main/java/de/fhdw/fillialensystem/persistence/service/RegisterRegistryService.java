package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.commons.api.dto.SystemClientDTO;
import de.fhdw.fillialensystem.utility.RegisterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegisterRegistryService {

    private static final Logger log = LoggerFactory.getLogger(RegisterRegistryService.class);
    private final Map<String, RegisterClient> kassensystemInstanceMap = new ConcurrentHashMap<>();

    public RegisterRegistryService() {
        super();
    }

    public Map<String, RegisterClient> getKassensystemInstanceMap() {
        return kassensystemInstanceMap;
    }

    public Optional<RegisterClient> findRegistryById(String id) {
        return Optional.ofNullable(kassensystemInstanceMap.get(id));
    }

    public List<RegisterClient> findAllRegistries() {
        return new ArrayList<>(kassensystemInstanceMap.values());
    }

    public List<RegisterClient> findAllActiveRegistries() {
        return new ArrayList<>(kassensystemInstanceMap.values().stream().filter(RegisterClient::isOnline).toList());
    }

    public void addRegistry(SystemClientDTO systemClientDTO) {
        if (kassensystemInstanceMap.containsKey(systemClientDTO.getId())) {
            throw new IllegalStateException("Kassensystem instance with id {%s} already exists".formatted(systemClientDTO.getId()));
        }

        RegisterClient instance = new RegisterClient(
                systemClientDTO,
                Instant.now(),
                Instant.now(),
                true
        );

        kassensystemInstanceMap.put(systemClientDTO.getId(), instance);
        log.atInfo().log("Kassensystem instance with id {%s} successfully registered".formatted(systemClientDTO.getId()));
    }

    public void deleteRegistry(String id) {
        if (kassensystemInstanceMap.remove(id) == null) {
            throw new IllegalArgumentException("Kassensystem instance with id {%s} not registered".formatted(id));
        } else {
            log.atInfo().log("Kassensystem instance with id {%s} successfully removed".formatted(id));
        }
    }
}