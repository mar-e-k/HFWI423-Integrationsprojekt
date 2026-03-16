package de.fhdw.vendix.store.core.persistance.account;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = SpringMapperConfig.class)
public interface AccountMapper extends EntityMapper<Account, AccountDTO> {

    @Override
    AccountDTO toDTO(Account entity);

    @Override
    Account toEntity(AccountDTO accountDTO);
}