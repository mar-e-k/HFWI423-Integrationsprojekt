package de.fhdw.fillialensystem.api.controller;

import de.fhdw.commons.api.controller.AccountAPI;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/account")
@Tag(name = "Account", description = "Endpoints for operations related to accounts")
public class AccountController implements AccountAPI {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    // ---- Endpoints ----

    @GetMapping
    @Operation(summary = "Retrieve all accounts")
    public ResponseEntity<List<AccountDTO>> getAccounts() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(findAll());
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Retrieve account by id")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(findById(id)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Retrieve account by uuid")
    public ResponseEntity<AccountDTO> getAccountByUuid(@PathVariable String uuid) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(findByUuid(uuid)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping("/name/{username}")
    @Operation(summary = "Retrieve account by username")
    public ResponseEntity<AccountDTO> getAccountByUsername(@PathVariable String username) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(findByUsername(username)
                        .orElseThrow(EntityNotFoundException::new));
    }

    // ---- API Helper Methods ----

    @Override
    public List<AccountDTO> findAll() {
        return accountService.findAll().stream().map(accountMapper::toDto).toList();
    }

    @Override
    public Optional<AccountDTO> findById(Long id) {
        return accountService.findById(id).map(accountMapper::toDto);
    }

    @Override
    public Optional<AccountDTO> findByUuid(String uuid) {
        return accountService.findByUuid(uuid).map(accountMapper::toDto);
    }

    @Override
    public Optional<AccountDTO> findByUsername(String username) {
        return accountService.findByUsername(username).map(accountMapper::toDto);
    }
}