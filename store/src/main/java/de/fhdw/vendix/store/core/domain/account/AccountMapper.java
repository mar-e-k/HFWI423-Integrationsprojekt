package de.fhdw.vendix.store.core.domain.account;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericEntityMapper;
import org.mapstruct.Mapper;

@Mapper
public interface AccountMapper extends GenericEntityMapper<Account, AccountDTO> {}