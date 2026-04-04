package de.fhdw.vendix.store.core.domain.receipt_voucher.service;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudQueryService;

import java.util.Optional;
import java.util.UUID;

interface ReceiptVoucherQueryService extends CrudQueryService<ReceiptVoucherDTO, Long> {
    Optional<ReceiptVoucherDTO> findByCode(UUID code);
}