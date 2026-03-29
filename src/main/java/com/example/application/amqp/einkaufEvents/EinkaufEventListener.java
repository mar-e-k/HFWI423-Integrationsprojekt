package com.example.application.amqp.einkaufEvents;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.contingent.ContingentRepository;
import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.data.orderPicking.KommissionPositionRepository;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.data.storageLocation.StorageLocationRepository;
import com.example.application.services.MessagingEventService;
import com.example.application.services.NewArticleNotificationService;
import io.github.plaguv.amqp.api.event.payment.DeleteQuotaEvent;
import io.github.plaguv.amqp.api.event.payment.NewQuotaEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import io.github.plaguv.amqp.core.listener.MessageRejectedException;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EinkaufEventListener {

    private static final Logger logger = LoggerFactory.getLogger(EinkaufEventListener.class);

    private final MessagingEventService messagingEventService;
    private final NewArticleNotificationService newArticleNotificationService;
    private final ContingentRepository contingentRepository;
    private final ArticleInfoRepository articleInfoRepo;
    private final MessageLogisticRepository messageLogisticRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final KommissionPositionRepository kommissionPositionRepository;

    public EinkaufEventListener(MessagingEventService messagingEventService,
                                NewArticleNotificationService newArticleNotificationService,
                                ContingentRepository contingentRepository,
                                ArticleInfoRepository articleInfoRepo,
                                MessageLogisticRepository messageLogisticRepository,
                                StorageLocationRepository storageLocationRepository,
                                KommissionPositionRepository kommissionPositionRepository) {
        this.messagingEventService = messagingEventService;
        this.newArticleNotificationService = newArticleNotificationService;
        this.contingentRepository = contingentRepository;
		this.articleInfoRepo = articleInfoRepo;
		this.messageLogisticRepository = messageLogisticRepository;
		this.storageLocationRepository = storageLocationRepository;
		this.kommissionPositionRepository = kommissionPositionRepository;
    }

    @AmqpEventListener
    public void onNewQuotaEvent(NewQuotaEvent event) {
        if (event == null) {
            throw new MessageRejectedException("NewQuotaEvent war null - ungültiges Event");
        }

        Long articleId = event.articleId();

        logger.info("Neue Quote/Budget erhalten - ArticleID: {}, Amount: {}",
                articleId, event.amount());

        MessagingEvent messagingEvent = new MessagingEvent(
                "NewQuota",
                articleId,
                Long.valueOf(event.amount()),
                "Neue Quote freigegeben"
        );
        messagingEventService.save(messagingEvent);

    }
    
    @Transactional
    @AmqpEventListener
    public void onDeleteQuotaEvent(DeleteQuotaEvent event) {
        if (event == null) {
            throw new MessageRejectedException("DeleteQuotaEvent war null - ungültiges Event");
        }

        logger.info("Starte vollständige Logistik-Löschung für ArticleID {}", event.articleId());

        //Alle Vorkommnisse entfernen
        deleteComissionEntrys(event.articleId());
        deleteOrderPickingEntrys(event.articleId());
        freeStorageLocations(event.articleId());
        deleteStocks(event.articleId());

        logger.info("Quote gelöscht/aufgehoben - ArticleID: {}", event.articleId());

        MessagingEvent messagingEvent = new MessagingEvent(
                "DeleteQuota",
                event.articleId(),
                null,
                "Quote gelöscht"
        );
        messagingEventService.save(messagingEvent);
    }

 
	//aus article info belegte fächer rausfinden und diese dann in storage location auf available setzen 
    private void freeStorageLocations(Long articleId) {
        ArticleInfo articleInfo = articleInfoRepo.findByArticleId(articleId);

        if (articleInfo == null) {
            logger.info("Kein ArticleInfo-Eintrag gefunden für ArticleID {} -> keine Lagerplätze freizugeben", articleId);
            return;
        }

        int freedCount = 0;

        freedCount += freeOneLocation(articleInfo.getStorageLocation());
        freedCount += freeOneLocation(articleInfo.getReserveStorageLocation());

        logger.info("{} Lagerplätze freigegeben für ArticleID {}", freedCount, articleId);
    }
    
    //article info einträge werden gelöscht - hoffentlich geht das trotz referenzen
	private void deleteStocks(Long articleId) {
	    long deletedCount = articleInfoRepo.deleteByArticleId(articleId);
	    logger.info("{} Bestandsdatensätze gelöscht für ArticleID {}", deletedCount, articleId);
	}
	//Einträge der gewünschten Kommissionierungen für diesen Artikel löschen
	private void deleteComissionEntrys(Long articleId) {
		ArticleInfo articleInfo = articleInfoRepo.findByArticleId(articleId);

	    if (articleInfo == null) {
	        logger.info("Kein ArticleInfo-Eintrag gefunden für ArticleID {} -> keine MessageLogistic-Einträge löschbar", articleId);
	        return;
	    }

	    String articleNumber = articleInfo.getArticleNumber();

	    long deletedCount = messageLogisticRepository.deleteByArticleNumber(articleNumber);
	    
	    logger.info("{} offene Filial-Bedarfe gelöscht für ArticleID {}", deletedCount, articleId);
	}
	//Einträge aus Order picking listen auch löschen damit nichts versucht wird loszuschicken
	private void deleteOrderPickingEntrys(Long articleId) {
		long deletedCount = kommissionPositionRepository.deleteByArticleId(articleId);
		logger.info("{} offene Filial-Bedarfe gelöscht für ArticleID {}", deletedCount, articleId);
	}
	
	//Hilfsmethode für freeStorageLocations
	private int freeOneLocation(String generalId) {
	    if (generalId == null || generalId.isBlank()) {
	        return 0;
	    }

	    try {
	        String[] parts = generalId.split("\\.");

	        String zonePart = parts[0];          // z.B. Z1
	        String shelfPart = parts[1];         // z.B. S2
	        String compartmentPart = parts[2];   // z.B. C3

	        String storageZone = "Zone " + zonePart.substring(1);
	        Integer shelfID = Integer.valueOf(shelfPart.substring(1));
	        Integer compartmentID = Integer.valueOf(compartmentPart.substring(1));

	        Optional<StorageLocation> locationOpt =
	                storageLocationRepository.findByStorageZoneAndShelfIDAndCompartmentID(
	                        storageZone, shelfID, compartmentID
	                );

	        if (locationOpt.isPresent()) {
	            StorageLocation location = locationOpt.get();
	            location.setStorageStatus("Available");
	            storageLocationRepository.save(location);
	            return 1;
	        }

	        logger.warn("Kein StorageLocation-Eintrag gefunden für {}", generalId);
	        return 0;

	    } catch (Exception e) {
	        logger.warn("Lagerplatz '{}' konnte nicht gelesen werden", generalId, e);
	        return 0;
	    }
	}
    
    
}
