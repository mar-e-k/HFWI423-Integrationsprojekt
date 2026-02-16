package de.fhdw.vendix.store.api.mapper;

import de.fhdw.vendix.commons.core.api.dto.ReceiptDTO;
import de.fhdw.vendix.commons.core.api.mapper.GenericMapper;
import de.fhdw.vendix.store.persistence.entity.Account;
import de.fhdw.vendix.store.persistence.entity.Receipt;
import de.fhdw.vendix.store.persistence.entity.Register;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("DuplicatedCode")
public class ReceiptMapper implements GenericMapper<Receipt, ReceiptDTO> {

    private final ReceiptLinkArticleMapper receiptLinkArticleMapper;

    public ReceiptMapper(ReceiptLinkArticleMapper receiptLinkArticleMapper) {
        this.receiptLinkArticleMapper = receiptLinkArticleMapper;
    }

    @Override
    public Receipt toEntity(ReceiptDTO dto) {
        Receipt receipt = new Receipt();
        receipt.setId(dto.getId());
        receipt.setRegister(new Register(dto.getAccountId()));
        receipt.setAccount(new Account(dto.getAccountId()));
        receipt.setTotalAmount(dto.getTotalAmount());
        receipt.setReceiptArticles(dto.getReceiptArticles().stream().map(receiptLinkArticleMapper::toEntity).toList());
        receipt.setDepositOnly(dto.isDepositOnly());
        receipt.setDepositRedemptionCode(dto.getDepositRedemptionCode()); // depositRedemptionCode hinzugefügt
        return receipt;
    }

    @Override
    public ReceiptDTO toDto(Receipt receipt) {
        ReceiptDTO dto = new ReceiptDTO();
        dto.setId(receipt.getId());
        dto.setStoreId(receipt.getStore().getId());
        dto.setRegisterId(receipt.getRegister().getId());
        dto.setAccountId(receipt.getAccount().getId());
        dto.setTotalAmount(receipt.getTotalAmount());
        dto.setReceiptArticles(receipt.getReceiptArticles().stream().map(receiptLinkArticleMapper::toDto).toList());
        dto.setDepositOnly(receipt.isDepositOnly());
        dto.setDepositRedemptionCode(receipt.getDepositRedemptionCode()); // depositRedemptionCode hinzugefügt
        return dto;
    }
}
