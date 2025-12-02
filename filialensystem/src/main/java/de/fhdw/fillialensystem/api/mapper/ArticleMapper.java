package de.fhdw.fillialensystem.api.mapper;

import de.fhdw.commons.api.dto.ArticleDTO;
import de.fhdw.commons.api.mapper.GenericMapper;
import de.fhdw.commons.utility.AuthContext;
import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkStock;
import de.fhdw.fillialensystem.persistence.entity.imported.Article;
import de.fhdw.fillialensystem.persistence.service.StoreLinkStockService;
import de.fhdw.fillialensystem.persistence.service.StoreService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@SuppressWarnings("DuplicatedCode")
public class ArticleMapper implements GenericMapper<Article, ArticleDTO> {

    private final StoreLinkStockService storeLinkStockService;
    private final StoreService storeService;

    public ArticleMapper(StoreLinkStockService storeLinkStockService, StoreService storeService) {
        this.storeLinkStockService = storeLinkStockService;
        this.storeService = storeService;
    }

    @Override
    public Article toEntity(ArticleDTO dto) {
        Article article = new Article();
        article.setId(dto.getId());
        article.setArticleNumber(dto.getArticleNumber());
        article.setDescription(dto.getDescription());
        article.setManufacturer(dto.getManufacturer());
        article.setName(dto.getName());
        article.setPurchasePrice(dto.getPurchasePrice());
        article.setSellingPrice(dto.getSellingPrice());
        // StockLevel wird hier nicht gesetzt, da es aus StoreLinkStock kommt
        article.setSupplier(dto.getSupplier());
        article.setTaxRatePercent(dto.getTaxRatePercent());
        article.setUnit(dto.getUnit());
        // isAvailable wird hier nicht gesetzt, da es aus StoreLinkStock kommt
        article.setHasDeposit(dto.isHasDeposit());
        return article;
    }

    @Override
    public ArticleDTO toDto(Article article) {
        ArticleDTO dto = new ArticleDTO();
        dto.setId(article.getId());
        dto.setArticleNumber(article.getArticleNumber());
        dto.setDescription(article.getDescription());
        dto.setManufacturer(article.getManufacturer());
        dto.setName(article.getName());
        dto.setPurchasePrice(article.getPurchasePrice());
        dto.setSellingPrice(article.getSellingPrice());
        dto.setSupplier(article.getSupplier());
        dto.setTaxRatePercent(article.getTaxRatePercent());
        dto.setUnit(article.getUnit());
        dto.setHasDeposit(article.isHasDeposit());

        // Hole die aktuelle Store-ID aus dem AuthContext
        AuthContext authContext = (AuthContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long storeId = authContext.getStoreId().longValue();

        if (storeId != null) {
            Optional<Store> storeOptional = storeService.findById(storeId);
            storeOptional.ifPresent(store -> {
                Optional<StoreLinkStock> storeLinkStockOptional = storeLinkStockService.findByStoreAndArticle(store, article);
                storeLinkStockOptional.ifPresent(storeLinkStock -> {
                    dto.setStockLevel(storeLinkStock.getAmount());
                    // Artikel ist nur verfügbar, wenn StoreLinkStock aktiv ist und der Bestand > 0 ist
                    dto.setAvailable(storeLinkStock.isActive() && storeLinkStock.getAmount() > 0);
                });
            });
        }
        // Wenn kein StoreLinkStock gefunden wird, ist der Artikel nicht verfügbar und der Bestand ist 0
        if (dto.getStockLevel() == null) {
            dto.setStockLevel(0);
            dto.setAvailable(false);
        }

        return dto;
    }
}
