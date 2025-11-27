package de.fhdw.commons.api.controller;

import de.fhdw.commons.api.dto.AccountDTO;

import java.util.List;
import java.util.Optional;

public interface AccountAPI {
    List<AccountDTO> findAll();
    Optional<AccountDTO> findById(Long id);
    Optional<AccountDTO> findByUuid(String uuid);
    Optional<AccountDTO> findByUsername(String username);
}