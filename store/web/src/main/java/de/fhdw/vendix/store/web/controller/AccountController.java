package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.account.web.AccountEndpoints;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.web.AccountQueryApi;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping(AccountEndpoints.BASE)
class AccountController {

    private final AccountQueryApi accountQueryApi;

    AccountController(AccountQueryApi accountQueryApi) {
        this.accountQueryApi = accountQueryApi;
    }

    @GetMapping(AccountEndpoints.BY_USERNAME)
    public ResponseEntity<AccountDTO> getAccountByUsername(@PathVariable String username) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountQueryApi.findByUsername(username)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping(AccountEndpoints.BY_UUID)
    public ResponseEntity<AccountDTO> getAccountByUuid(@PathVariable UUID uuid) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountQueryApi.findByUUID(uuid)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping(AccountEndpoints.BY_PHONE)
    public ResponseEntity<AccountDTO> getAccountByPhone(@PathVariable String phone) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountQueryApi.findByPhone(phone)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping(AccountEndpoints.BY_EMAIL)
    public ResponseEntity<AccountDTO> getAccountByEmail(@PathVariable String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountQueryApi.findByEmail(email)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping(AccountEndpoints.ROLE)
    public ResponseEntity<Set<AccountRoleEnum>> findAllRoles(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountQueryApi.findAllRoles(id));
    }
}