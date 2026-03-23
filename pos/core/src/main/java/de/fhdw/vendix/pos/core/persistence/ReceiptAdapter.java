package de.fhdw.vendix.pos.core.persistence;

import org.springframework.stereotype.Service;

@Service
public class ReceiptAdapter {

//    private final CartItemMapper cartItemMapper;
//
//    public ReceiptAdapter(StoreClient storeClient, CartItemMapper cartItemMapper) {
//        super(storeClient);
//        this.cartItemMapper = cartItemMapper;
//    }
//
//    public Optional<ReceiptDTO> createReceipt(List<ReceiptLinkArticleDTO> receiptArticles) {
//        return getWebClient()
//                .post()
//                .uri("/api/receipt")
//                .bodyValue(receiptArticles)
//                .retrieve()
//                .bodyToMono(ReceiptDTO.class)
//                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
//                .blockOptional();
//    }
//
//    public Optional<ReceiptDTO> createReceiptFromCartItems(List<CartItem> cartItems) {
//        return createReceipt(cartItems.stream().map(cartItemMapper::toDto).toList());
//    }
//
//    public Optional<ReceiptDTO> redeemDepositReceipt(String depositRedemptionCode) {
//        return getWebClient()
//                .post()
//                .uri("/api/receipt/redeem/{depositRedemptionCode}", depositRedemptionCode)
//                .retrieve()
//                .bodyToMono(ReceiptDTO.class)
//                .onErrorResume(WebClientResponseException.class, e -> Mono.empty())
//                .blockOptional();
//    }
}
