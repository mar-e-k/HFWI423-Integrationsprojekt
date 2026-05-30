package de.fhdw.vendix.pos.ui.register;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterStateTest {

    @Test
    void updatesSelectionsAndNotifiesListeners() {
        RegisterState state = new RegisterState();
        AtomicInteger calls = new AtomicInteger();
        ArticleDTO article = new ArticleDTO(1L, "12345678", "Milk", "Fresh milk", "ACME", "Supplier", "pcs",
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(19), 10L, true, false);

        state.addListener(calls::incrementAndGet);
        state.setSelectedArticle(article);
        state.setAmount(3);

        assertThat(state.getSelectedArticle()).contains(article);
        assertThat(state.getAmount()).isEqualTo(3);
        assertThat(calls).hasValue(2);
    }

    @Test
    void nullSelectionClearsSelectedArticle() {
        RegisterState state = new RegisterState();

        state.setSelectedArticle(null);

        assertThat(state.getSelectedArticle()).isEmpty();
    }
}
