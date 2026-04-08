package de.fhdw.vendix.orchestrator.core.domain.register;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.register.RegisterRequestDTO;
import de.fhdw.vendix.commons.api.domain.register.RegisterResponseDTO;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import de.fhdw.vendix.orchestrator.core.domain.store.StoreDTOMapper;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                StoreDTOMapper.class,
        }
)
public interface RegisterDTOMapper extends DTOMapper<RegisterDTO, RegisterRequestDTO, RegisterResponseDTO> {

    @Override
    RegisterDTO toDomainDTO(RegisterRequestDTO dto);

    @Override
    RegisterResponseDTO toResponseDTO(RegisterDTO registerDTO);

    @Override
    Set<RegisterDTO> toDomainDTOs(Iterable<RegisterRequestDTO> requestDTOs);

    @Override
    Set<RegisterResponseDTO> toResponseDTOs(Iterable<RegisterDTO> domainDTOs);
}