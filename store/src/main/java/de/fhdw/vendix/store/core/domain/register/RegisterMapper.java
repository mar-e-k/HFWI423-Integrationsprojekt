package de.fhdw.vendix.store.core.domain.register;

import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RegisterMapper extends EntityMapper<Register, RegisterDTO> {

    RegisterMapper INSTANCE = Mappers.getMapper(RegisterMapper.class);

    @Override
    RegisterDTO toDTO(Register entity);

    @Override
    Register toEntity(RegisterDTO registerDTO);
}