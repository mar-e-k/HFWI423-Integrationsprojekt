package de.fhdw.commons.api.controller;

import de.fhdw.commons.api.dto.AccountDTO;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;

import java.util.List;
import java.util.Optional;

public interface AccountApi {
    List<AccountDTO> findAll();
    Optional<AccountDTO> findById(Long id);
    Optional<AccountDTO> findByUuid(String uuid);
    boolean existsByAccountRole_Role(AccountRoleEnum role);
}