package de.fhdw.fillialensystem.api.mapper;

import de.fhdw.commons.api.dto.ReceiptDTO;
import de.fhdw.commons.api.mapper.GenericMapper;
import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.Receipt;
import de.fhdw.fillialensystem.persistence.entity.Register;
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
        return dto;
    }
}
