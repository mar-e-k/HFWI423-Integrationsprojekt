package fhdw.de.einkauf_service.amqp;

import fhdw.de.einkauf_service.entity.ReceivedDealNotification;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.repository.ReceivedDealNotificationRepository;
import io.github.plaguv.amqp.api.event.logistic.NewDealEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EinkaufEventListenerTest {

    @Mock
    private ReceivedDealNotificationRepository notificationRepository;

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private EinkaufEventListener listener;

    @Test
    void onNewDealEvent_articleFound_savesNotificationWithRealData() {
        when(notificationRepository.existsByExternalEventId(anyString())).thenReturn(false);
        when(articleRepository.findArticleNumberById(5L)).thenReturn(Optional.of("12345678"));
        when(articleRepository.findNameById(5L)).thenReturn(Optional.of("TestArtikel"));

        listener.onNewDealEvent(new NewDealEvent(5L));

        ArgumentCaptor<ReceivedDealNotification> captor =
                ArgumentCaptor.forClass(ReceivedDealNotification.class);
        verify(notificationRepository).save(captor.capture());

        ReceivedDealNotification saved = captor.getValue();
        assertThat(saved.getArticleId()).isEqualTo(5L);
        assertThat(saved.getArticleNumber()).isEqualTo("12345678");
        assertThat(saved.getArticleName()).isEqualTo("TestArtikel");
        assertThat(saved.getReceivedAt()).isNotNull();
        assertThat(saved.getExternalEventId()).isNotBlank();
    }

    @Test
    void onNewDealEvent_articleNumberNotFound_usesFallbackGtinPrefix() {
        when(notificationRepository.existsByExternalEventId(anyString())).thenReturn(false);
        when(articleRepository.findArticleNumberById(7L)).thenReturn(Optional.empty());
        when(articleRepository.findNameById(7L)).thenReturn(Optional.of("Bier"));

        listener.onNewDealEvent(new NewDealEvent(7L));

        ArgumentCaptor<ReceivedDealNotification> captor =
                ArgumentCaptor.forClass(ReceivedDealNotification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getArticleNumber()).isEqualTo("GTIN-7");
    }

    @Test
    void onNewDealEvent_articleNameNotFound_usesFallbackArtikelPrefix() {
        when(notificationRepository.existsByExternalEventId(anyString())).thenReturn(false);
        when(articleRepository.findArticleNumberById(7L)).thenReturn(Optional.of("12345678"));
        when(articleRepository.findNameById(7L)).thenReturn(Optional.empty());

        listener.onNewDealEvent(new NewDealEvent(7L));

        ArgumentCaptor<ReceivedDealNotification> captor =
                ArgumentCaptor.forClass(ReceivedDealNotification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getArticleName()).isEqualTo("Artikel #7");
    }

    @Test
    void onNewDealEvent_bothFallbacks_correctFallbackStrings() {
        when(notificationRepository.existsByExternalEventId(anyString())).thenReturn(false);
        when(articleRepository.findArticleNumberById(9L)).thenReturn(Optional.empty());
        when(articleRepository.findNameById(9L)).thenReturn(Optional.empty());

        listener.onNewDealEvent(new NewDealEvent(9L));

        ArgumentCaptor<ReceivedDealNotification> captor =
                ArgumentCaptor.forClass(ReceivedDealNotification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getArticleNumber()).isEqualTo("GTIN-9");
        assertThat(captor.getValue().getArticleName()).isEqualTo("Artikel #9");
    }

    @Test
    void onNewDealEvent_nullEvent_doesNotCallSave() {
        listener.onNewDealEvent(null);

        verify(notificationRepository, never()).save(any());
        verify(notificationRepository, never()).existsByExternalEventId(anyString());
    }

    @Test
    void onNewDealEvent_happyPath_saveCalledOnce() {
        when(notificationRepository.existsByExternalEventId(anyString())).thenReturn(false);
        when(articleRepository.findArticleNumberById(1L)).thenReturn(Optional.of("11111111"));
        when(articleRepository.findNameById(1L)).thenReturn(Optional.of("Artikel"));

        listener.onNewDealEvent(new NewDealEvent(1L));

        verify(notificationRepository, times(1)).save(any(ReceivedDealNotification.class));
    }

    @Test
    void onNewDealEvent_duplicateExternalEventId_skipsSave() {
        when(notificationRepository.existsByExternalEventId(anyString())).thenReturn(true);

        listener.onNewDealEvent(new NewDealEvent(42L));

        verify(notificationRepository, never()).save(any(ReceivedDealNotification.class));
        verify(articleRepository, never()).findArticleNumberById(anyLong());
        verify(articleRepository, never()).findNameById(anyLong());
    }
}
