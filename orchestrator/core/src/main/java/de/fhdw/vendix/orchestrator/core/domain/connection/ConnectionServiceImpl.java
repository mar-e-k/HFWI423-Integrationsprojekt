package de.fhdw.vendix.orchestrator.core.domain.connection;

import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.data.crud.AbstractCrudService;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class ConnectionServiceImpl extends AbstractCrudService<Connection, Long> implements ConnectionService {

    private static final Logger log = LoggerFactory.getLogger(ConnectionServiceImpl.class);

    private final ConnectionRepository connectionRepository;

    ConnectionServiceImpl(ConnectionRepository connectionRepository) {
        super(connectionRepository);
        this.connectionRepository = connectionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Connection> findAllByType(TargetType type) {
        if (type == null) {
            return List.of();
        }
        return connectionRepository.findAllByTarget_Type(type);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Connection> findByTarget(EntityTarget target) {
        if (target == null) {
            return Optional.empty();
        }
        return connectionRepository.findByTarget(target);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Connection> findByInstanceUUID(UUID instanceUuid) {
        if (instanceUuid == null) {
            return Optional.empty();
        }
        return connectionRepository.findByInstance_Uuid(instanceUuid);
    }

    @Override
    @Transactional
    public void deleteByTarget(EntityTarget target) {
        log.atInfo().log("[DELETE] Deleting connection by target: {}", target);
        if (target == null) {
            throw new IllegalArgumentException("Parameter 'target' cannot be null");
        }
        connectionRepository.deleteByTarget(target);
        log.atInfo().log("[DELETE] Successfully deleted connection by target: {}", target);
    }


    @Override
    @Transactional
    public void deleteByInstanceUUID(UUID instanceUUID) {
        log.atInfo().log("[DELETE] Deleting connection by instanceUuid: {}", instanceUUID);
        if (instanceUUID == null) {
            throw new IllegalArgumentException("Parameter 'instanceUuid' cannot be null");
        }
        connectionRepository.deleteByInstance_Uuid(instanceUUID);
        log.atInfo().log("[DELETE] Successfully deleted connection by instanceUuid: {}", instanceUUID);
    }
}