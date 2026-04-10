package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleDTO;
import de.fhdw.vendix.commons.spring.web.server.orchestrator.api.AccountApi;
import de.fhdw.vendix.orchestrator.core.domain.account.AccountMapper;
import de.fhdw.vendix.orchestrator.core.domain.account.AccountService;
import de.fhdw.vendix.orchestrator.core.domain.account_role.AccountRoleMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
class AccountController implements AccountApi {

    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private final AccountRoleMapper accountRoleMapper;

    AccountController(AccountService accountService, AccountMapper accountMapper, AccountRoleMapper accountRoleMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
        this.accountRoleMapper = accountRoleMapper;
    }

    @Override
    public ResponseEntity<AccountDTO> getAccountById(Long id) {
        Optional<AccountDTO> account = accountService.findById(id).map(accountMapper::toDTO);
        return ResponseEntity.of(account);
    }

    @Override
    public ResponseEntity<AccountDTO> getAccountByUuid(UUID uuid) {
        Optional<AccountDTO> account = accountService.findByUuid(uuid).map(accountMapper::toDTO);
        return ResponseEntity.of(account);
    }

    @Override
    public ResponseEntity<AccountDTO> getAccountByUsername(String username) {
        Optional<AccountDTO> account = accountService.findByUsername(username).map(accountMapper::toDTO);
        return ResponseEntity.of(account);
    }

    @Override
    public ResponseEntity<List<AccountRoleDTO>> getAccountRolesById(Long id) {
        List<AccountRoleDTO> roles = accountService.findAllRolesById(id).stream()
                .map(accountRoleMapper::toDTO)
                .toList();
        return ResponseEntity.ok(roles);
    }
}