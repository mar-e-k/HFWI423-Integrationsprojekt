package de.fhdw.fillialensystem.api.mapper;

import de.fhdw.commons.api.dto.ArticleDTO;
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
        Article entity = new Article();
        entity.setId(dto.getId());
        entity.setArticleNumber(dto.getArticleNumber());
        entity.setDescription(dto.getDescription());
        entity.setManufacturer(dto.getManufacturer());
        entity.setName(dto.getName());
        entity.setPurchasePrice(dto.getPurchasePrice());
        entity.setSellingPrice(dto.getSellingPrice());
        entity.setStockLevel(dto.getStockLevel());
        entity.setSupplier(dto.getSupplier());
        entity.setTaxRatePercent(dto.getTaxRatePercent());
        entity.setUnit(dto.getUnit());
        entity.setIsAvailable(dto.getAvailable());
        return entity;
    }

    @Override
    public ArticleDTO toDto(Article entity) {
        ArticleDTO dto = new ArticleDTO();
        dto.setId(entity.getId());
        dto.setArticleNumber(entity.getArticleNumber());
        dto.setDescription(entity.getDescription());
        dto.setManufacturer(entity.getManufacturer());
        dto.setName(entity.getName());
        dto.setPurchasePrice(entity.getPurchasePrice());
        dto.setSellingPrice(entity.getSellingPrice());
        dto.setStockLevel(entity.getStockLevel());
        dto.setSupplier(entity.getSupplier());
        dto.setTaxRatePercent(entity.getTaxRatePercent());
        dto.setUnit(entity.getUnit());
        dto.setAvailable(entity.getIsAvailable());
        return dto;
    }
}