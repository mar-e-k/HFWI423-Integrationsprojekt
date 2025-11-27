package de.fhdw.fillialensystem.api.mapper;

import de.fhdw.commons.api.dto.AccountDTO;
import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.AccountRole;
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
        account.setAccountRole(new AccountRole(
                null,
                dto.getRole()));
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