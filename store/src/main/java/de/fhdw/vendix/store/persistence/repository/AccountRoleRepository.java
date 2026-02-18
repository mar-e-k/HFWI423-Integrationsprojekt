package de.fhdw.vendix.store.persistence.repository;

import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.store.persistence.entity.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRoleRepository extends JpaRepository<AccountRole,Long> {
    Optional<AccountRole> findByRole(AccountRoleEnum role);
}