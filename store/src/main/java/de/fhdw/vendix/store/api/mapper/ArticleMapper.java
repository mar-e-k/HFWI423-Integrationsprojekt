package de.fhdw.vendix.store.api.mapper;

import de.fhdw.vendix.commons.core.api.dto.ArticleDTO;
import de.fhdw.vendix.commons.core.api.mapper.GenericMapper;
import de.fhdw.vendix.commons.security.auth.AuthContextHolder;
import de.fhdw.vendix.store.persistence.entity.Store;
import de.fhdw.vendix.store.persistence.entity.StoreLinkStock;
import de.fhdw.vendix.store.persistence.entity.imported.Article;
import de.fhdw.vendix.store.persistence.service.StoreLinkStockService;
import de.fhdw.vendix.store.persistence.service.StoreService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@SuppressWarnings("DuplicatedCode")
public class ArticleMapper implements GenericMapper<Article, ArticleDTO> {

    // TODO: Refactor Mapper Logic out of Mapper
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
        article.setSupplier(dto.getSupplier());
        article.setTaxRatePercent(dto.getTaxRatePercent());
        article.setUnit(dto.getUnit());
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

        Long storeId = AuthContextHolder.current().isPresent() ? AuthContextHolder.current().get().getStoreId() : null;

        if (storeId != null) {
            Optional<Store> storeOptional = storeService.findById(storeId);
            storeOptional.ifPresent(store -> {
                Optional<StoreLinkStock> storeLinkStockOptional = storeLinkStockService.findByStoreAndArticle(store, article);
                storeLinkStockOptional.ifPresent(storeLinkStock -> {
                    dto.setStockLevel(storeLinkStock.getAmount());
                    dto.setAvailable(storeLinkStock.isActive() && storeLinkStock.getAmount() > 0);
                });
            });
        }
        if (dto.getStockLevel() == null) {
            dto.setStockLevel(0);
            dto.setAvailable(false);
        }

        return dto;
    }
}
