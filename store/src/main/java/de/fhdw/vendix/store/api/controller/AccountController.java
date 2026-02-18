package de.fhdw.vendix.store.api.controller;

import de.fhdw.vendix.commons.api.account.AccountEndpoints;
import de.fhdw.vendix.commons.core.api.dto.AccountDTO;
import de.fhdw.vendix.store.api.mapper.AccountMapper;
import de.fhdw.vendix.store.persistence.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AccountEndpoints.BASE)
@Tag(name = "Account", description = "Endpoints for operations related to accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @GetMapping(AccountEndpoints.BY_ID)
    @Operation(summary = "Retrieve account by id")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.findById(id)
                        .map(accountMapper::toDto)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping(AccountEndpoints.BY_UUID)
    @Operation(summary = "Retrieve account by uuid")
    public ResponseEntity<AccountDTO> getAccountByUuid(@PathVariable String uuid) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.findByUuid(uuid)
                        .map(accountMapper::toDto)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping(AccountEndpoints.BY_USERNAME)
    @Operation(summary = "Retrieve account by username")
    public ResponseEntity<AccountDTO> getAccountByUsername(@PathVariable String username) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.findByUsername(username)
                        .map(accountMapper::toDto)
                        .orElseThrow(EntityNotFoundException::new));
    }
}