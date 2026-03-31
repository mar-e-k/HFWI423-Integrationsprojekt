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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EinkaufEventListenerTest {

    @Mock
    private ReceivedDealNotificationRepository notificationRepository;

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private EinkaufEventListener listener;

    // --- onNewDealEvent — happy path (article found) ---

    @Test
    void onNewDealEvent_articleFound_savesNotificationWithRealData() {
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
    }

    // --- onNewDealEvent — fallback when article data missing ---

    @Test
    void onNewDealEvent_articleNumberNotFound_usesFallbackGtinPrefix() {
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
        when(articleRepository.findArticleNumberById(9L)).thenReturn(Optional.empty());
        when(articleRepository.findNameById(9L)).thenReturn(Optional.empty());

        listener.onNewDealEvent(new NewDealEvent(9L));

        ArgumentCaptor<ReceivedDealNotification> captor =
                ArgumentCaptor.forClass(ReceivedDealNotification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getArticleNumber()).isEqualTo("GTIN-9");
        assertThat(captor.getValue().getArticleName()).isEqualTo("Artikel #9");
    }

    // --- onNewDealEvent — null safety ---

    @Test
    void onNewDealEvent_nullEvent_doesNotCallSave() {
        listener.onNewDealEvent(null);

        verify(notificationRepository, never()).save(any());
    }

    // --- onNewDealEvent — save is called exactly once ---

    @Test
    void onNewDealEvent_happyPath_saveCalledOnce() {
        when(articleRepository.findArticleNumberById(1L)).thenReturn(Optional.of("11111111"));
        when(articleRepository.findNameById(1L)).thenReturn(Optional.of("Artikel"));

        listener.onNewDealEvent(new NewDealEvent(1L));

        verify(notificationRepository, times(1)).save(any(ReceivedDealNotification.class));
    }
}
