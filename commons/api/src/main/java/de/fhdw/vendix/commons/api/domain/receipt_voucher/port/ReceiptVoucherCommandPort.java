package de.fhdw.vendix.commons.api.domain.receipt_voucher.port;

import de.fhdw.vendix.commons.api.structure.port.CommandPort;

import java.util.UUID;

public interface ReceiptVoucherCommandPort extends CommandPort {
    void redeemCode(UUID code);
}