package de.fhdw.vendix.commons.api.domain.receipt_voucher.port;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.dto.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptVoucherQueryPort extends QueryPort {
    Optional<ReceiptVoucherDTO> findByCode(UUID code);
}