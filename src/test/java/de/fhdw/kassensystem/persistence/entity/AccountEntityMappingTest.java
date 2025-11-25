package de.fhdw.kassensystem.persistence.entity;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AccountEntityMappingTest {

    /**
     * SCRUM-37 – Benutzerrollenverwaltung:
     * Verifiziert, dass ein Account genau eine (nicht-nullbare) ManyToOne-Beziehung
     * zu AccountRole besitzt und damit jedem Benutzer genau eine Rolle zugeordnet ist.
     */
    @Test
    void accountHasExactlyOneRole_viaManyToOneMapping() throws Exception {
        Field field = Account.class.getDeclaredField("accountRole");
        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);

        assertNotNull(manyToOne, "Feld 'accountRole' muss mit @ManyToOne annotiert sein.");
        assertNotNull(joinColumn, "Feld 'accountRole' muss eine @JoinColumn besitzen.");
        assertFalse(joinColumn.nullable(), "Account muss genau eine (nicht nullbare) Rolle besitzen.");

        long roleMappings = Arrays.stream(Account.class.getDeclaredFields())
                .filter(f -> f.getType().equals(AccountRole.class))
                .filter(f -> f.getAnnotation(ManyToOne.class) != null)
                .count();

        assertEquals(1, roleMappings,
                "Account darf genau eine ManyToOne-Beziehung auf AccountRole besitzen.");
    }
}