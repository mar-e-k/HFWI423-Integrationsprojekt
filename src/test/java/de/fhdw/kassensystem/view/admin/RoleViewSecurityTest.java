package de.fhdw.kassensystem.view.admin;

import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import jakarta.annotation.security.RolesAllowed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoleViewSecurityTest {

    /**
     * SCRUM-32 – Benutzerrollen und Berechtigungen:
     * Prüft, dass die Rollenverwaltungs-Oberfläche nur für ROLE_ADMIN zugänglich ist
     * und normale Benutzer keinen Zugriff auf diese Funktion erhalten.
     */
    @Test
    void roleView_isRestrictedToAdminsViaRolesAllowedAnnotation() {
        RolesAllowed rolesAllowed = RoleView.class.getAnnotation(RolesAllowed.class);

        assertNotNull(rolesAllowed, "RoleView muss mit @RolesAllowed abgesichert sein.");

        List<String> allowed = List.of(rolesAllowed.value());

        assertAll(
                () -> assertTrue(allowed.contains(AccountRoleEnum.ROLE_ADMIN),
                        "RoleView muss für ROLE_ADMIN zugänglich sein."),
                () -> assertEquals(1, allowed.size(),
                        "RoleView darf ausschließlich für ROLE_ADMIN freigeschaltet sein.")
        );
    }
}