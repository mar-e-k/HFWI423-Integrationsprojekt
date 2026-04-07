package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleDTO;
import de.fhdw.vendix.commons.spring.web.server.orchestrator.api.AccountApi;
import de.fhdw.vendix.orchestrator.core.domain.account.AccountMapper;
import de.fhdw.vendix.orchestrator.core.domain.account.AccountService;
import de.fhdw.vendix.orchestrator.core.domain.account_role.AccountRoleMapper;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

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
    public ResponseEntity<Set<AccountDTO>> getAccounts(
            @Nullable Long id,
            @Nullable UUID uuid,
            @Nullable String username,
            @Nullable String password,
            @Nullable String firstName,
            @Nullable String middleName,
            @Nullable String lastName,
            @Nullable String phone,
            @Nullable String email
    ) {
        Set<AccountDTO> filtered = accountService.findAll().stream()
                .filter(a -> id == null || Objects.equals(a.getId(), id))
                .filter(a -> uuid == null || Objects.equals(a.getUuid(), uuid))
                .filter(a -> username == null || Objects.equals(a.getUsername(), username))
                .filter(a -> password == null || Objects.equals(a.getPassword(), password))
                .filter(a -> firstName == null || Objects.equals(a.getFirstName(), firstName))
                .filter(a -> middleName == null || Objects.equals(a.getMiddleName(), middleName))
                .filter(a -> lastName == null || Objects.equals(a.getLastName(), lastName))
                .filter(a -> phone == null || Objects.equals(a.getPhone(), phone))
                .filter(a -> email == null || Objects.equals(a.getEmail(), email))
                .map(accountMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(filtered);
    }

    @Override
    public ResponseEntity<AccountDTO> getAccountByUsername(String username) {
        Optional<AccountDTO> account = accountService.findByUsername(username).map(accountMapper::toDTO);
        return ResponseEntity.of(account);
    }

    @Override
    public ResponseEntity<AccountDTO> getAccountByUUID(UUID uuid) {
        Optional<AccountDTO> account = accountService.findByUuid(uuid).map(accountMapper::toDTO);
        return ResponseEntity.of(account);
    }

    @Override
    public ResponseEntity<Set<AccountRoleDTO>> getAccountRoles(UUID uuid) {
        Set<AccountRoleDTO> roles = accountService.findByUuid(uuid)
                .map(a -> accountRoleMapper.toDTOs(
                        accountService.findAllRoles(a.getUuid())
                ))
                .orElseGet(Set::of);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roles);
    }
}