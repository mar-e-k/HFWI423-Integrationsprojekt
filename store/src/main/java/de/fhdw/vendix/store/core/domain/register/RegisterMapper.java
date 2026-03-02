package de.fhdw.vendix.store.core.domain.register;

import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;

@Mapper
public interface RegisterMapper extends EntityMapper<Register, RegisterDTO> {}