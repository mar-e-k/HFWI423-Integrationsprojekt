package de.fhdw.vendix.orchestrator.core.domain.account;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface AccountMapper extends EntityMapper<Account, AccountDTO> {

    @Override
    AccountDTO toDTO(Account entity);

    @Override
    Account toEntity(AccountDTO accountDTO);

    @Override
    List<AccountDTO> toDTOs(Iterable<Account> entities);

    @Override
    List<Account> toEntities(Iterable<AccountDTO> accountDTOS);
}