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
        dto.setStockLevel(article.getStockLevel());
        dto.setSupplier(article.getSupplier());
        dto.setTaxRatePercent(article.getTaxRatePercent());
        dto.setUnit(article.getUnit());
        dto.setAvailable(article.getIsAvailable());
        dto.setHasDeposit(article.isHasDeposit());
        return dto;
    }
}
