package de.fhdw.vendix.orchestrator.core.domain.connection;

import de.fhdw.vendix.commons.api.domain.connection.ConnectionDTO;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.orchestrator.core.domain.connection.service.ConnectionService;
import org.springframework.stereotype.Service;

@Service
class ConnectionAdapter extends AbstractDtoCrudAdapter<Connection, ConnectionDTO, Long> implements ConnectionService {

    private final ConnectionEntityAdapter connectionEntityAdapter;
    private final ConnectionMapper connectionMapper;

    ConnectionAdapter(ConnectionEntityAdapter connectionEntityAdapter, ConnectionMapper connectionMapper) {
        super(connectionEntityAdapter, connectionMapper);
        this.connectionEntityAdapter = connectionEntityAdapter;
        this.connectionMapper = connectionMapper;
    }
}