package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.account.web.AccountEndpoints;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.web.AccountQueryApi;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping(AccountEndpoints.BASE)
@Tag(name = "Account", description = "Endpoints for operations related to accounts")
class AccountController {

    private final AccountQueryApi accountQueryApi;

    AccountController(AccountQueryApi accountQueryApi) {
        this.accountQueryApi = accountQueryApi;
    }

    @GetMapping(AccountEndpoints.BY_USERNAME)
    @Operation(summary = "Retrieve account by accountUsername")
    public ResponseEntity<AccountDTO> getAccountByUsername(@PathVariable String username) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountQueryApi.findByUsername(username)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping(AccountEndpoints.BY_UUID)
    @Operation(summary = "Retrieve account by subject")
    public ResponseEntity<AccountDTO> getAccountByUuid(@PathVariable UUID uuid) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountQueryApi.findByUUID(uuid)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping(AccountEndpoints.BY_PHONE)
    @Operation(summary = "Retrieve account by subject")
    public ResponseEntity<AccountDTO> getAccountByPhone(@PathVariable String phone) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountQueryApi.findByPhone(phone)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping(AccountEndpoints.BY_EMAIL)
    @Operation(summary = "Retrieve account by subject")
    public ResponseEntity<AccountDTO> getAccountByEmail(@PathVariable String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountQueryApi.findByEmail(email)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping(AccountEndpoints.ROLE)
    @Operation(summary = "Retrieve all account roles by account id")
    public ResponseEntity<Set<AccountRoleEnum>> findAllRoles(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountQueryApi.findAllRoles(id));
    }
}