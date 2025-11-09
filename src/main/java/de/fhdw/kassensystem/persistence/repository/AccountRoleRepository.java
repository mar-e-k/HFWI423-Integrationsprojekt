package de.fhdw.kassensystem.persistence.repository;

import de.fhdw.kassensystem.persistence.entity.AccountRole;
import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRoleRepository extends JpaRepository<AccountRole,Long> {
    Optional<AccountRole> findByRole(AccountRoleEnum role);
}