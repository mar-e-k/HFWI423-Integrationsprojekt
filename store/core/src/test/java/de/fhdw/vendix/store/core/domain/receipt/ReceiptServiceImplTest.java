package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.PaymentMethod;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptStatus;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLineService;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStockService;
import de.fhdw.vendix.store.core.domain.voucher.VoucherService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"NullAway", "unchecked"})
class ReceiptServiceImplTest {

    private static final UUID CASHIER = UUID.fromString("77777777-7777-4777-8777-777777777777");

    @Test
    void checkoutPersistsLinesWithCreatedReceiptId() throws ReceiptAlreadyCheckedOutException {
        ReceiptRepository receiptRepository = mock(ReceiptRepository.class);
        ReceiptLineService receiptLineService = mock(ReceiptLineService.class);
        StoreStockService storeStockService = mock(StoreStockService.class);
        VoucherService voucherService = mock(VoucherService.class);
        Receipt created = new Receipt(42L, 1L, 2L, CASHIER, PaymentMethod.CARD, ReceiptStatus.OPEN);
        when(receiptRepository.save(any(Receipt.class))).thenReturn(created);

        Receipt receipt = new Receipt(1L, 2L, CASHIER, PaymentMethod.CARD, ReceiptStatus.OPEN);
        ReceiptLine line = line(999L, 5L, 2L);

        Receipt result = new ReceiptServiceImpl(receiptRepository, receiptLineService, storeStockService, voucherService)
                .checkoutReceipt(receipt, List.of(line), List.of());

        assertThat(result.getId()).isEqualTo(42L);
        verify(receiptLineService).createAll(org.mockito.ArgumentMatchers.argThat(lines -> {
            List<ReceiptLine> persisted = (List<ReceiptLine>) lines;
            return persisted.size() == 1 && persisted.getFirst().getReceiptId().equals(42L);
        }));
        verify(storeStockService).decrementArticles(1L, java.util.Map.of(5L, 2L));
    }

    @Test
    void checkoutRejectsEmptyLinesAndDifferentTransientReceiptIds() {
        ReceiptServiceImpl service = new ReceiptServiceImpl(mock(ReceiptRepository.class), mock(ReceiptLineService.class),
                mock(StoreStockService.class), mock(VoucherService.class));
        Receipt receipt = new Receipt(1L, 2L, CASHIER, PaymentMethod.CARD, ReceiptStatus.OPEN);

        assertThatThrownBy(() -> service.checkoutReceipt(receipt, List.of(), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.checkoutReceipt(receipt,
                List.of(line(1L, 5L, 1L), line(2L, 5L, 1L)), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelReceiptRestoresStockAndUpdatesReceipt() {
        ReceiptRepository receiptRepository = mock(ReceiptRepository.class);
        Receipt receipt = new Receipt(42L, 1L, 2L, CASHIER, PaymentMethod.CASH, ReceiptStatus.OPEN);
        when(receiptRepository.findById(42L)).thenReturn(java.util.Optional.of(receipt));
        when(receiptRepository.findAllReceiptLinesByReceiptId(42L)).thenReturn(List.of(line(42L, 7L, 3L)));
        when(receiptRepository.existsById(42L)).thenReturn(true);
        when(receiptRepository.save(receipt)).thenReturn(receipt);
        StoreStockService storeStockService = mock(StoreStockService.class);

        Receipt cancelled = new ReceiptServiceImpl(receiptRepository, mock(ReceiptLineService.class), storeStockService,
                mock(VoucherService.class)).cancelReceipt(42L);

        assertThat(cancelled.getStatus()).isEqualTo(ReceiptStatus.CANCELLED);
        verify(storeStockService).incrementArticles(1L, java.util.Map.of(7L, 3L));
    }

    private static ReceiptLine line(Long receiptId, Long articleId, Long amount) {
        try {
            var constructor = ReceiptLine.class.getDeclaredConstructor(Long.class, Long.class, Long.class);
            constructor.setAccessible(true);
            return constructor.newInstance(receiptId, articleId, amount);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
