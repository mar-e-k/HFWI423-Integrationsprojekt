package de.fhdw.vendix.store.api.mapper;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericEntityMapper;
import de.fhdw.vendix.store.persistence.entity.Account;
import org.mapstruct.Mapper;

@Mapper
public interface AccountMapper extends GenericEntityMapper<Account, AccountDTO> {}