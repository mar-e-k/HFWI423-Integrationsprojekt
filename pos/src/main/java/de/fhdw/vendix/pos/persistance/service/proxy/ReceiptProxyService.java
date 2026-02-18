package de.fhdw.vendix.pos.persistance.service.proxy;

import de.fhdw.vendix.commons.core.api.dto.ReceiptDTO;
import de.fhdw.vendix.commons.core.api.dto.ReceiptLinkArticleDTO;
import de.fhdw.vendix.pos.rest.api.mapper.CartItemMapper;
import de.fhdw.vendix.pos.utility.StoreClient;
import de.fhdw.vendix.pos.view.cashier.CartItem;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class ReceiptProxyService extends AbstractProxyService{

    private final CartItemMapper cartItemMapper;

    public ReceiptProxyService(StoreClient storeClient, CartItemMapper cartItemMapper) {
        super(storeClient);
        this.cartItemMapper = cartItemMapper;
    }

    public Optional<ReceiptDTO> createReceipt(List<ReceiptLinkArticleDTO> receiptArticles) {
        return getWebClient()
                .post()
                .uri("/api/receipt")
                .bodyValue(receiptArticles)
                .retrieve()
                .bodyToMono(ReceiptDTO.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .blockOptional();
    }

    public Optional<ReceiptDTO> createReceiptFromCartItems(List<CartItem> cartItems) {
        return createReceipt(cartItems.stream().map(cartItemMapper::toDto).toList());
    }

    public Optional<ReceiptDTO> redeemDepositReceipt(String depositRedemptionCode) {
        return getWebClient()
                .post()
                .uri("/api/receipt/redeem/{depositRedemptionCode}", depositRedemptionCode)
                .retrieve()
                .bodyToMono(ReceiptDTO.class)
                .onErrorResume(WebClientResponseException.class, e -> Mono.empty())
                .blockOptional();
    }
}
