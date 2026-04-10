package de.fhdw.vendix.orchestrator.core.domain.distributed_lock;

import de.fhdw.vendix.commons.api.domain.distributed_lock.DistributedLockDTO;
import de.fhdw.vendix.commons.api.domain.distributed_lock.DistributedLockRequestDTO;
import de.fhdw.vendix.commons.api.domain.distributed_lock.DistributedLockResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface DistributedLockDTOMapper extends DTOMapper<DistributedLockDTO, DistributedLockRequestDTO, DistributedLockResponseDTO> {

    @Override
    DistributedLockDTO toDomainDTO(DistributedLockRequestDTO dto);

    @Override
    DistributedLockResponseDTO toResponseDTO(DistributedLockDTO distributedLockDTO);

    @Override
    List<DistributedLockDTO> toDomainDTOs(Iterable<DistributedLockRequestDTO> requestDTOs);

    @Override
    List<DistributedLockResponseDTO> toResponseDTOs(Iterable<DistributedLockDTO> domainDTOs);
}