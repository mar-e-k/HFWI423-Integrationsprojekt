package de.fhdw.kassensystem.view.cashier;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CashierViewTest {

    /**
     * SCRUM-65 – Manuelle Preisänderung bei fehlendem Verkaufspreis:
     * Stellt sicher, dass die Konstante MIN_PRICE mindestens 0,01 € beträgt
     * und damit negative oder ungültige Preiseingaben verhindert werden.
     */
    @Test
    void minPriceConstant_isAtLeastOneCent() throws Exception {
        Field field = CashierView.class.getDeclaredField("MIN_PRICE");
        field.setAccessible(true);

        BigDecimal minPrice = (BigDecimal) field.get(null);
        BigDecimal expected = new BigDecimal("0.01");

        assertEquals(0, expected.compareTo(minPrice),
                "MIN_PRICE muss 0,01 € sein, um negative/ungültige Preise zu verhindern.");
    }
}