package de.fhdw.vendix.store.persistence.service.other;

import de.fhdw.vendix.commons.core.api.dto.SystemClientDTO;
import de.fhdw.vendix.store.persistence.entity.Register;
import de.fhdw.vendix.store.persistence.service.RegisterService;
import de.fhdw.vendix.store.utility.RegisterClient;
import de.fhdw.vendix.store.utility.StoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final StoreClient storeClient;
    private final RegisterService registerService;

    public RegisterRegistryService(StoreClient storeClient, RegisterService registerService) {
        this.storeClient = storeClient;
        this.registerService = registerService;
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

    @Transactional
    public void addRegistry(SystemClientDTO systemClientDTO) {
        if (kassensystemInstanceMap.containsKey(systemClientDTO.getId())) {
            throw new IllegalStateException("Kassensystem instance with id {%s} already exists".formatted(systemClientDTO.getId()));
        }

        List<Register> registers = registerService.findAllByStore(storeClient.getStore());

        if (registers.isEmpty()) {
            throw new IllegalStateException("Store has no registers defined");
        }

        for (Register register : registers) {
            boolean exists = findAllActiveRegistries().stream()
                    .anyMatch(rc -> rc.getRegister().equals(register));

            if (!exists) {
                RegisterClient instance = new RegisterClient(
                        systemClientDTO,
                        register,
                        Instant.now(),
                        Instant.now(),
                        true
                );

                kassensystemInstanceMap.put(systemClientDTO.getId(), instance);
                log.atInfo().log("Kassensystem instance with id {%s} successfully registered for register {%s}"
                        .formatted(systemClientDTO.getId(), register.getId()));
                return;
            }
        }

        throw new IllegalStateException("Register cannot be registered because there is no register available anymore");
    }

    public void deleteRegistry(String id) {
        if (kassensystemInstanceMap.remove(id) == null) {
            throw new IllegalArgumentException("Kassensystem instance with id {%s} not registered".formatted(id));
        } else {
            log.atInfo().log("Kassensystem instance with id {%s} successfully removed".formatted(id));
        }
    }
}