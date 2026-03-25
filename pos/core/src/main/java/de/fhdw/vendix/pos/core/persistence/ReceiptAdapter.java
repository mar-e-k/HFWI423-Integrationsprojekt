package de.fhdw.vendix.pos.core.persistence;

import de.fhdw.vendix.commons.api.domain.article.web.ArticleCommandApi;
import de.fhdw.vendix.commons.api.domain.article.web.ArticleQueryApi;
import de.fhdw.vendix.commons.spring.web.AbstractApiClient;
import de.fhdw.vendix.security.api.jwt.JwtService;
import de.fhdw.vendix.security.api.jwt.payload.JwtPayload;
import org.springframework.stereotype.Service;

@Service
class ReceiptAdapter extends AbstractApiClient implements ArticleQueryApi, ArticleCommandApi {

    protected ReceiptAdapter(JwtService jwtService) {
        super(jwtService, "localhost:8080");
    }

    @Override
    protected JwtPayload buildJwtPayload() {
        return null;
    }

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
