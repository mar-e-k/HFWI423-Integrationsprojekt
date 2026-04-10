package de.fhdw.vendix.orchestrator.core.domain.register;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface RegisterMapper extends EntityMapper<Register, RegisterDTO> {

    @Override
    RegisterDTO toDTO(Register entity);

    @Override
    Register toEntity(RegisterDTO registerDTO);

    @Override
    List<RegisterDTO> toDTOs(Iterable<Register> entities);

    @Override
    List<Register> toEntities(Iterable<RegisterDTO> registerDTOS);
}