package de.fhdw.vendix.store.core.domain.register;

import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericEntityMapper;
import org.mapstruct.Mapper;

@Mapper
public interface RegisterMapper extends GenericEntityMapper<Register, RegisterDTO> {}