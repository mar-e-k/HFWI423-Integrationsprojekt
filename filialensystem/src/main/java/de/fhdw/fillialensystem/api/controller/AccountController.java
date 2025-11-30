package de.fhdw.fillialensystem.api.controller;

import de.fhdw.commons.api.dto.AccountDTO;
import de.fhdw.fillialensystem.api.mapper.AccountMapper;
import de.fhdw.fillialensystem.persistence.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
@Tag(name = "Account", description = "Endpoints for operations related to accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @GetMapping
    @Operation(summary = "Retrieve all accounts")
    public ResponseEntity<List<AccountDTO>> getAccounts() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.findAll()
                        .stream()
                        .map(accountMapper::toDto)
                        .toList());
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Retrieve account by id")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.findById(id)
                        .map(accountMapper::toDto)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Retrieve account by uuid")
    public ResponseEntity<AccountDTO> getAccountByUuid(@PathVariable String uuid) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.findByUuid(uuid)
                        .map(accountMapper::toDto)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping("/name/{username}")
    @Operation(summary = "Retrieve account by username")
    public ResponseEntity<AccountDTO> getAccountByUsername(@PathVariable String username) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.findByUsername(username)
                        .map(accountMapper::toDto)
                        .orElseThrow(EntityNotFoundException::new));
    }
}