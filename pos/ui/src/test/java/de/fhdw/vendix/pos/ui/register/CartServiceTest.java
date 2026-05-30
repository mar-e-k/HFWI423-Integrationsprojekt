package de.fhdw.vendix.pos.ui.register;

import com.vaadin.flow.spring.security.AuthenticationContext;
import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.receipt.PaymentMethod;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptResponseDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptStatus;
import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.spring.app.context.register.RegisterContext;
import de.fhdw.vendix.commons.spring.web.api.store.ReceiptApi;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("NullAway")
class CartServiceTest {

    private static final UUID CASHIER = UUID.fromString("88888888-8888-4888-8888-888888888888");

    @Test
    void addLineMergesSameArticle() {
        CartService service = new CartService(mock(RegisterContext.class), mock(ReceiptApi.class),
                mock(AuthenticationContext.class));

        service.addLine(article(1L), 2);
        service.addLine(article(1L), 3);

        assertThat(service.getCartLines()).hasSize(1);
        assertThat(service.getCartLines().getFirst().line().articleAmount()).isEqualTo(5L);
    }

    @Test
    void checkoutBuildsRequestAndClearsCart() {
        RegisterContext registerContext = mock(RegisterContext.class);
        ReceiptApi receiptApi = mock(ReceiptApi.class);
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        OidcUser cashier = mock(OidcUser.class);
        when(cashier.getSubject()).thenReturn(CASHIER.toString());
        when(authenticationContext.getAuthenticatedUser(OidcUser.class)).thenReturn(Optional.of(cashier));
        when(registerContext.getRegister()).thenReturn(new RegisterDTO(1L, 7L));
        ReceiptResponseDTO response = new ReceiptResponseDTO(99L, 7L, 1L, CASHIER, PaymentMethod.CARD, ReceiptStatus.OPEN);
        when(receiptApi.checkoutReceipt(any())).thenReturn(ResponseEntity.ok(response));
        CartService service = new CartService(registerContext, receiptApi, authenticationContext);
        service.addLine(article(1L), 2);

        assertThat(service.checkout(PaymentMethod.CARD)).isEqualTo(response);
        assertThat(service.getCartLines()).isEmpty();
    }

    @Test
    void checkoutRejectsEmptyCart() {
        CartService service = new CartService(mock(RegisterContext.class), mock(ReceiptApi.class),
                mock(AuthenticationContext.class));

        assertThatThrownBy(() -> service.checkout(PaymentMethod.CARD))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static ArticleDTO article(Long id) {
        return new ArticleDTO(id, "12345678", "Milk", "Fresh milk", "ACME", "Supplier", "pcs",
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(19), 10L, true, false);
    }
}
