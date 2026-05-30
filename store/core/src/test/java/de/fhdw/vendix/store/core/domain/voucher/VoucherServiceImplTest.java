package de.fhdw.vendix.store.core.domain.voucher;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NullAway")
class VoucherServiceImplTest {

    @Test
    void findByVoucherCodeRejectsNullAndDelegatesValidCode() {
        VoucherRepository repository = mock(VoucherRepository.class);
        Voucher voucher = new Voucher(1L, UUID.fromString("44444444-4444-4444-8444-444444444444"));
        when(repository.findByCode(voucher.getCode())).thenReturn(Optional.of(voucher));

        VoucherServiceImpl service = new VoucherServiceImpl(repository);

        assertThat(service.findByVoucherCode(null)).isEmpty();
        assertThat(service.findByVoucherCode(voucher.getCode())).contains(voucher);
    }

    @Test
    void redeemCodeMarksVoucherRedeemedAndPersistsIt() {
        VoucherRepository repository = mock(VoucherRepository.class);
        UUID code = UUID.fromString("55555555-5555-4555-8555-555555555555");
        Voucher voucher = new Voucher(9L, code, Instant.now().plusSeconds(60), null);
        when(repository.findByCode(code)).thenReturn(Optional.of(voucher));
        when(repository.existsById(9L)).thenReturn(true);
        when(repository.save(voucher)).thenReturn(voucher);

        new VoucherServiceImpl(repository).redeemCode(code);

        assertThat(voucher.getRedeemedAt()).isNotNull();
        verify(repository).save(voucher);
    }

    @Test
    void redeemCodeFailsForMissingVoucher() {
        VoucherRepository repository = mock(VoucherRepository.class);
        UUID code = UUID.fromString("66666666-6666-4666-8666-666666666666");
        when(repository.findByCode(code)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new VoucherServiceImpl(repository).redeemCode(code))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
