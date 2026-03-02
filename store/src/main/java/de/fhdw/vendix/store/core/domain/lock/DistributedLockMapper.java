package de.fhdw.vendix.store.core.domain.lock;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericEntityMapper;
import org.mapstruct.Mapper;

@Mapper
public interface DistributedLockMapper extends GenericEntityMapper<DistributedLock, LockDTO> {}