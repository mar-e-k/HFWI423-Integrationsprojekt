package de.fhdw.vendix.commons.api.domain.receipt_voucher.port;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.dto.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.structure.port.CommandPort;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptVoucherCommandPort extends CommandPort {
    Optional<ReceiptVoucherDTO> findByCode(UUID code);

    void redeemCode(UUID code);
}