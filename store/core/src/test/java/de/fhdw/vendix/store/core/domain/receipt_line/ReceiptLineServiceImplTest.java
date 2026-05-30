package de.fhdw.vendix.store.core.domain.receipt_line;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NullAway")
class ReceiptLineServiceImplTest {

    @Test
    void createAllUsesBulkRepository() {
        ReceiptLineRepository repository = mock(ReceiptLineRepository.class);
        ReceiptLineBulkRepository bulkRepository = mock(ReceiptLineBulkRepository.class);
        List<ReceiptLine> lines = List.of(new ReceiptLine(1L, 2L, 3L));
        when(bulkRepository.bulkInsert(lines)).thenReturn(lines);

        List<ReceiptLine> created = new ReceiptLineServiceImpl(repository, bulkRepository).createAll(lines);

        assertThat(created).isSameAs(lines);
        verify(bulkRepository).bulkInsert(lines);
    }

    @Test
    void createAllRejectsNullEntitiesAndExistingIds() {
        ReceiptLineServiceImpl service = new ReceiptLineServiceImpl(mock(ReceiptLineRepository.class),
                mock(ReceiptLineBulkRepository.class));

        assertThatThrownBy(() -> service.createAll(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.createAll(List.of(new ReceiptLine(7L, 1L, 2L, 3L))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
