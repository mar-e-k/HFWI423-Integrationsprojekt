package de.fhdw.vendix.store.core.domain.account;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountMapper extends EntityMapper<Account, AccountDTO> {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    @Override
    AccountDTO toDTO(Account entity);

    @Override
    Account toEntity(AccountDTO accountDTO);
}