package de.fhdw.vendix.store.core.persistance.receipt_voucher.port;

import de.fhdw.vendix.commons.api.structure.service.CommandService;

import java.util.UUID;

interface ReceiptVoucherCommandService extends CommandService {
    void redeemCode(UUID code);
}