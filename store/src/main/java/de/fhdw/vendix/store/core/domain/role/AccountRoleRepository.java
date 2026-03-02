package de.fhdw.vendix.store.core.domain.role;

import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRoleRepository extends JpaRepository<AccountRole,Long> {
    Optional<AccountRole> findByRole(AccountRoleEnum role);
}