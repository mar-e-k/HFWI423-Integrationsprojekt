package de.fhdw.vendix.orchestrator.core.domain.register;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import de.fhdw.vendix.orchestrator.core.domain.store.StoreMapper;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                StoreMapper.class
        }
)
public interface RegisterMapper extends EntityMapper<Register, RegisterDTO> {

    @Override
    RegisterDTO toDTO(Register entity);

    @Override
    Register toEntity(RegisterDTO registerDTO);

    @Override
    Set<RegisterDTO> toDTOs(Iterable<Register> entities);

    @Override
    Set<Register> toEntities(Iterable<RegisterDTO> registerDTOS);
}