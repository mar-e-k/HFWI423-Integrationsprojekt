package de.fhdw.kassensystem.persistance.service.proxy;

import de.fhdw.commons.api.dto.ReceiptDTO;
import de.fhdw.commons.api.dto.ReceiptLinkArticleDTO;
import de.fhdw.kassensystem.rest.api.mapper.CartItemMapper;
import de.fhdw.kassensystem.utility.StoreClient;
import de.fhdw.kassensystem.view.cashier.CartItem;
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
        return storeClient.getWebClient()
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
        return storeClient.getWebClient()
                .post()
                .uri("/api/receipt/redeem/{depositRedemptionCode}", depositRedemptionCode)
                .retrieve()
                .bodyToMono(ReceiptDTO.class)
                .onErrorResume(WebClientResponseException.class, e -> Mono.empty())
                .blockOptional();
    }
}
