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
        Article account = new Article();
        account.setId(dto.getId());
        account.setArticleNumber(dto.getArticleNumber());
        account.setDescription(dto.getDescription());
        account.setManufacturer(dto.getManufacturer());
        account.setName(dto.getName());
        account.setPurchasePrice(dto.getPurchasePrice());
        account.setSellingPrice(dto.getSellingPrice());
        account.setStockLevel(dto.getStockLevel());
        account.setSupplier(dto.getSupplier());
        account.setTaxRatePercent(dto.getTaxRatePercent());
        account.setUnit(dto.getUnit());
        account.setIsAvailable(dto.getAvailable());
        return account;
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