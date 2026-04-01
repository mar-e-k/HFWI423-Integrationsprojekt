package de.fhdw.vendix.store.core.persistance.register.port;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudQueryService;

interface RegisterQueryService extends CrudQueryService<RegisterDTO, Long> {}