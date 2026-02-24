package de.fhdw.vendix.store.api.mapper;

import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericEntityMapper;
import de.fhdw.vendix.store.persistence.entity.Register;
import org.mapstruct.Mapper;

@Mapper
public interface RegisterMapper extends GenericEntityMapper<Register, RegisterDTO> {}