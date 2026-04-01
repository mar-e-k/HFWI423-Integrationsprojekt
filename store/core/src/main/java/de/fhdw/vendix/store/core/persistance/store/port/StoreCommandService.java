package de.fhdw.vendix.store.core.persistance.store.port;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudCommandService;

interface StoreCommandService extends CrudCommandService<StoreDTO, Long> {}