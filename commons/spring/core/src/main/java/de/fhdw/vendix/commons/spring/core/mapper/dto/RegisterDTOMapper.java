package de.fhdw.vendix.commons.spring.core.mapper.dto;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterRequestDTO;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterResponseDTO;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

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
    List<RegisterDTO> toDomainDTOs(Iterable<RegisterRequestDTO> requestDTOs);

    @Override
    List<RegisterResponseDTO> toResponseDTOs(Iterable<RegisterDTO> domainDTOs);
}