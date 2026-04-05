package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.spring.web.server.orchestrator.api.AccountApi;
import de.fhdw.vendix.orchestrator.core.domain.account.service.AccountService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
class AccountController implements AccountApi {

    private final AccountService accountService;

    AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public ResponseEntity<List<AccountDTO>> getAccounts(
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
        List<AccountDTO> filtered = accountService.findAll().stream()
                .filter(a -> id == null || Objects.equals(a.id(), id))
                .filter(a -> uuid == null || Objects.equals(a.uuid(), uuid))
                .filter(a -> username == null || Objects.equals(a.username(), username))
                .filter(a -> password == null || Objects.equals(a.password(), password))
                .filter(a -> firstName == null || Objects.equals(a.firstName(), firstName))
                .filter(a -> middleName == null || Objects.equals(a.middleName(), middleName))
                .filter(a -> lastName == null || Objects.equals(a.lastName(), lastName))
                .filter(a -> phone == null || Objects.equals(a.phone(), phone))
                .filter(a -> email == null || Objects.equals(a.email(), email))
                .toList();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(filtered);
    }
}