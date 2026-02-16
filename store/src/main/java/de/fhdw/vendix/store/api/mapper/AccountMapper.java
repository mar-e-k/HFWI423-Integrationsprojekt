package de.fhdw.vendix.store.api.mapper;

import de.fhdw.vendix.commons.core.api.dto.AccountDTO;
import de.fhdw.vendix.commons.core.api.mapper.GenericMapper;
import de.fhdw.vendix.store.persistence.entity.Account;
import de.fhdw.vendix.store.persistence.entity.AccountRole;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("DuplicatedCode")
public class AccountMapper implements GenericMapper<Account, AccountDTO> {

    public AccountMapper() {
        super();
    }

    @Override
    public Account toEntity(AccountDTO dto) {
        Account account = new Account();
        account.setId(dto.getId());
        account.setAccountRole(new AccountRole(dto.getRole()));
        account.setUuid(dto.getUuid());
        account.setUsername(dto.getUsername());
        account.setPassword(dto.getPassword());
        return account;
    }

    @Override
    public AccountDTO toDto(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setRole(account.getAccountRole().getRole());
        dto.setUuid(account.getUuid());
        dto.setUsername(account.getUsername());
        dto.setPassword(account.getPassword());
        return dto;
    }
}