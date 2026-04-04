package de.fhdw.vendix.orchestrator.core.domain.connection;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import org.springframework.stereotype.Service;

@Service
class ConnectionEntityAdapter extends AbstractEntityCrudAdapter<Connection, Long> {

    private final ConnectionRepository connectionRepository;

    ConnectionEntityAdapter(ConnectionRepository connectionRepository) {
        super(connectionRepository);
        this.connectionRepository = connectionRepository;
    }
}