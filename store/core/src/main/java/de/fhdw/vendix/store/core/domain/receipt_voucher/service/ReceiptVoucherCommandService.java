package de.fhdw.vendix.store.core.domain.receipt_voucher.service;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudCommandService;

import java.util.UUID;

interface ReceiptVoucherCommandService extends CrudCommandService<ReceiptVoucherDTO, Long> {
    void redeemCode(UUID code);
}