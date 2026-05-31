package com.example.application.services;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.orderPicking.*;
import com.example.application.events.KommissionAbgeschlossenEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KommissionServiceTest {

    @Mock private KommissionRepository komRepo;
    @Mock private KommissionPositionRepository posRepo;
    @Mock private ArticleInfoService artikelService;
    @Mock private MessageLogisticRepository msgRepo;
    @Mock private ArticleInfoRepository articleRepo;
    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private KommissionService service;

    @Test
    void finishAtomar_publishesKommissionAbgeschlossenEvent() {
        // Arrange
        Kommission k = new Kommission();
        k.setStoreId("1");

        ArticleInfo article = new ArticleInfo();
        article.setId(99L);
        article.setArticleNumber("ART-001");
        article.setStockLevel(20);
        article.setPiecesPerPallet(10);
        article.setReservePallets(0);

        MessageLogistic msg = new MessageLogistic();
        msg.setArticleId(99L);
        msg.setArticleNumber("ART-001");
        msg.setQuantity(5L);

        when(msgRepo.findByKommissionIdForUpdate(any())).thenReturn(List.of(msg));
        when(articleRepo.findAllById(anyList())).thenReturn(List.of(article));
        when(komRepo.save(any())).thenReturn(k);

        // Act
        service.finishAtomar(k);

        // Assert: ApplicationEvent wird publiziert, NICHT LogisticEventPublisher direkt
        ArgumentCaptor<KommissionAbgeschlossenEvent> captor =
            ArgumentCaptor.forClass(KommissionAbgeschlossenEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        KommissionAbgeschlossenEvent event = captor.getValue();
        assertThat(event.getStoreId()).isEqualTo(1L);
        assertThat(event.getDeliveries()).hasSize(1);
        assertThat(event.getDeliveries().get(0).articleId()).isEqualTo(99L);
        assertThat(event.getDeliveries().get(0).quantity()).isEqualTo(5L);
    }

    @Test
    void finishAtomar_doesNotPublishEventWhenNoDeliveries() {
        Kommission k = new Kommission();
        k.setStoreId("store-1");

        when(msgRepo.findByKommissionIdForUpdate(any())).thenReturn(List.of());
        when(articleRepo.findAllById(anyList())).thenReturn(List.of());
        when(komRepo.save(any())).thenReturn(k);

        service.finishAtomar(k);

        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}
