package de.fhdw.vendix.commons.api.domain.lock;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockRequestDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericDTOMapper;
import org.mapstruct.Mapper;

@Mapper
public interface LockDTOMapper extends GenericDTOMapper<LockDTO, LockRequestDTO, LockResponseDTO> {}