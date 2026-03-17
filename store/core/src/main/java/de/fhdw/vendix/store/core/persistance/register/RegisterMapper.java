package de.fhdw.vendix.store.core.persistance.register;

import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import de.fhdw.vendix.store.core.persistance.store.StoreMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {StoreMapper.class}
)
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