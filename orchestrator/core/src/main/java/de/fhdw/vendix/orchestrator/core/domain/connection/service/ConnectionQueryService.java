package de.fhdw.vendix.orchestrator.core.domain.connection.service;

import de.fhdw.vendix.commons.api.domain.connection.ConnectionDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudQueryService;

interface ConnectionQueryService extends CrudQueryService<ConnectionDTO, Long> {}