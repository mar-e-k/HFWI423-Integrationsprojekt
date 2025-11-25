package de.fhdw.kassensystem.persistence;

import de.fhdw.kassensystem.persistence.entity.AccountRole;
import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import de.fhdw.kassensystem.persistence.repository.AccountRoleRepository;
import de.fhdw.kassensystem.persistence.service.AccountRoleService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountRoleServiceTest {

    /**
     * SCRUM-32 – Benutzerrollen und Berechtigungen:
     * Prüft, dass der AccountRoleService Rollen direkt aus der Datenbank liest,
     * sodass Änderungen an Berechtigungen nach dem Speichern sofort wirksam werden.
     */
    @Test
    void accountRoleService_findByRoleDelegatesToRepository() {
        AccountRoleRepository repo = Mockito.mock(AccountRoleRepository.class);
        AccountRoleService service = new AccountRoleService(repo);

        AccountRole role = new AccountRole();
        role.setRole(AccountRoleEnum.ADMIN);

        when(repo.findByRole(AccountRoleEnum.ADMIN)).thenReturn(Optional.of(role));

        Optional<AccountRole> result = service.findByRole(AccountRoleEnum.ADMIN);

        assertAll(
                () -> assertTrue(result.isPresent(),
                        "AccountRoleService muss Rollen aus dem Repository liefern."),
                () -> assertEquals(AccountRoleEnum.ADMIN, result.get().getRole(),
                        "AccountRoleService muss die korrekte Rolle zurückgeben."),
                () -> assertSame(role, result.get(),
                        "Service muss exakt das vom Repository gelieferte Objekt zurückgeben.")
        );

        verify(repo, times(1)).findByRole(AccountRoleEnum.ADMIN);
        verifyNoMoreInteractions(repo);
    }
}