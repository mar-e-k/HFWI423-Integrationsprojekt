package de.fhdw.vendix.store.api.mapper;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericEntityMapper;
import de.fhdw.vendix.store.persistence.entity.DistributedLock;
import org.mapstruct.Mapper;

@Mapper
public interface DistributedLockMapper extends GenericEntityMapper<DistributedLock, LockDTO> {}