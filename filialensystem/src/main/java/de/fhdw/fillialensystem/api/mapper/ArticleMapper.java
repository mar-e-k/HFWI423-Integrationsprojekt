package de.fhdw.fillialensystem.api.mapper;

import de.fhdw.commons.api.dto.ArticleDTO;
import de.fhdw.commons.api.mapper.GenericMapper;
import de.fhdw.fillialensystem.persistence.entity.imported.Article;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("DuplicatedCode")
public class ArticleMapper implements GenericMapper<Article, ArticleDTO> {

    public ArticleMapper() {
        super();
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
        article.setStockLevel(dto.getStockLevel());
        article.setSupplier(dto.getSupplier());
        article.setTaxRatePercent(dto.getTaxRatePercent());
        article.setUnit(dto.getUnit());
        article.setIsAvailable(dto.getAvailable());
        return article;
    }

    @Override
    public ArticleDTO toDto(Article account) {
        ArticleDTO dto = new ArticleDTO();
        dto.setId(account.getId());
        dto.setArticleNumber(account.getArticleNumber());
        dto.setDescription(account.getDescription());
        dto.setManufacturer(account.getManufacturer());
        dto.setName(account.getName());
        dto.setPurchasePrice(account.getPurchasePrice());
        dto.setSellingPrice(account.getSellingPrice());
        dto.setStockLevel(account.getStockLevel());
        dto.setSupplier(account.getSupplier());
        dto.setTaxRatePercent(account.getTaxRatePercent());
        dto.setUnit(account.getUnit());
        dto.setAvailable(account.getIsAvailable());
        return dto;
    }
}