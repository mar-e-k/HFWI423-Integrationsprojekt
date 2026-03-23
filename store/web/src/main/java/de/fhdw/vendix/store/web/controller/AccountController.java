package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.account.web.AccountEndpoints;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(AccountEndpoints.BASE)
@Tag(name = "Account", description = "Endpoints for operations related to accounts")
class AccountController {

    private final AccountQueryPort accountQueryPort;

    public AccountController(AccountQueryPort accountQueryPort) {
        this.accountQueryPort = accountQueryPort;
    }

    @GetMapping(AccountEndpoints.BY_ID)
    @Operation(summary = "Retrieve account by id")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountQueryPort.findById(id)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping(AccountEndpoints.BY_UUID)
    @Operation(summary = "Retrieve account by subject")
    public ResponseEntity<AccountDTO> getAccountByUuid(@PathVariable UUID uuid) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountQueryPort.findByUUID(uuid)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping(AccountEndpoints.BY_USERNAME)
    @Operation(summary = "Retrieve account by accountUsername")
    public ResponseEntity<AccountDTO> getAccountByUsername(@PathVariable String username) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountQueryPort.findByUsername(username)
                        .orElseThrow(EntityNotFoundException::new));
    }
}