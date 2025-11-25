package de.fhdw.kassensystem.persistence.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountRoleEnumTest {

    /**
     * SCRUM-32 – Benutzerrollen und Berechtigungen:
     * Prüft, dass die ADMIN-Rolle die Authority „ROLE_ADMIN“ besitzt und damit
     * korrekt in Spring Security verwendet werden kann.
     */
    @Test
    void accountRoleEnum_getAuthorityHasRolePrefix() {
        AccountRoleEnum adminRole = AccountRoleEnum.ADMIN;
        String authority = adminRole.getAuthority();

        assertEquals("ROLE_" + adminRole.name(), authority,
                "ADMIN-Rolle muss Authority 'ROLE_ADMIN' (ROLE_ + Enum-Name) besitzen.");
    }

    /**
     * SCRUM-32 – Benutzerrollen und Berechtigungen:
     * Stellt sicher, dass alle Rollen-Authorities mit dem Prefix „ROLE_“ beginnen,
     * wie es Spring Security für Rollen erwartet.
     */
    @Test
    void allAccountRoles_haveProperRolePrefix() {
        for (AccountRoleEnum role : AccountRoleEnum.values()) {
            assertTrue(role.getAuthority().startsWith("ROLE_"),
                    role + " muss mit ROLE_ anfangen.");
        }
    }
}