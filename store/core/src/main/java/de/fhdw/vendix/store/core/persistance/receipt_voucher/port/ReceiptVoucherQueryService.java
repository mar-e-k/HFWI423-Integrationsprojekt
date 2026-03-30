package de.fhdw.vendix.store.core.persistance.receipt_voucher.port;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.structure.service.QueryService;

import java.util.Optional;
import java.util.UUID;

interface ReceiptVoucherQueryService extends QueryService {
    Optional<ReceiptVoucherDTO> findByCode(UUID code);
}