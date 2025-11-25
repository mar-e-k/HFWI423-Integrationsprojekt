package de.fhdw.fillialensystem.api.registry;

import de.fhdw.commons.api.dto.SystemClientDTO;
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
public class KassensystemRegistryService {

    private static final Logger log = LoggerFactory.getLogger(KassensystemRegistryService.class);
    private final Map<String, KassensystemInstance> kassensystemInstanceMap = new ConcurrentHashMap<>();

    public KassensystemRegistryService() {
        super();
    }

    public Map<String, KassensystemInstance> getKassensystemInstanceMap() {
        return kassensystemInstanceMap;
    }

    public Optional<KassensystemInstance> findRegistryById(String id) {
        return Optional.ofNullable(kassensystemInstanceMap.get(id));
    }

    public List<KassensystemInstance> findAllRegistries() {
        return new ArrayList<>(kassensystemInstanceMap.values());
    }

    public void addRegistry(SystemClientDTO systemClientDTO) {
        if (kassensystemInstanceMap.containsKey(systemClientDTO.getId())) {
            throw new IllegalStateException("Kassensystem instance with id {%s} already exists".formatted(systemClientDTO.getId()));
        }

        KassensystemInstance instance = new  KassensystemInstance(
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