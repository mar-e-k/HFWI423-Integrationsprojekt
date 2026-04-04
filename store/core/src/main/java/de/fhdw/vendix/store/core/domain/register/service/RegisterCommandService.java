package de.fhdw.vendix.store.core.domain.register.service;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudCommandService;

interface RegisterCommandService extends CrudCommandService<RegisterDTO, Long> {}