package de.fhdw.fillialensystem.persistence.repository;

import de.fhdw.fillialensystem.persistence.entity.AccountRole;
import de.fhdw.fillialensystem.persistence.entity.AccountRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRoleRepository extends JpaRepository<AccountRole,Long> {
    Optional<AccountRole> findByRole(AccountRoleEnum role);
}