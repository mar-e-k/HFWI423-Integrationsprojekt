package com.example.application.amqp.einkaufEvents;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.contingent.ContingentRepository;
import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.data.orderPicking.KommissionPositionRepository;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.data.storageLocation.StorageLocationRepository;
import com.example.application.services.BadgeNotifier;
import com.example.application.services.MessagingEventService;
import io.github.plaguv.amqp.api.event.payment.DeleteQuotaEvent;
import io.github.plaguv.amqp.api.event.payment.NewQuotaEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import io.github.plaguv.amqp.core.listener.MessageRejectedException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

@Component
public class EinkaufEventListener {

	private static final Logger logger = LoggerFactory.getLogger(EinkaufEventListener.class);

	private final MessagingEventService messagingEventService;
	private final BadgeNotifier badgeNotifier;
	private final ContingentRepository contingentRepository;
	private final ArticleInfoRepository articleInfoRepo;
	private final MessageLogisticRepository messageLogisticRepository;
	private final StorageLocationRepository storageLocationRepository;
	private final KommissionPositionRepository kommissionPositionRepository;

	public EinkaufEventListener(MessagingEventService messagingEventService,
								BadgeNotifier badgeNotifier,
								ContingentRepository contingentRepository,
								ArticleInfoRepository articleInfoRepo,
								MessageLogisticRepository messageLogisticRepository,
								StorageLocationRepository storageLocationRepository,
								KommissionPositionRepository kommissionPositionRepository) {
		this.messagingEventService = messagingEventService;
		this.badgeNotifier = badgeNotifier;
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
		logger.info("Neue Quote erhalten - ArticleID: {}, Amount: {}", articleId, event.amount());

		MessagingEvent messagingEvent = new MessagingEvent(
				"NewQuota",
				articleId,
				Long.valueOf(event.amount()),
				"Neue Quote freigegeben"
		);
		messagingEventService.save(messagingEvent);

		// Asynchron warten bis der neue Eintrag in der DB sichtbar ist
		badgeNotifier.notifyAfterNewQuota();
	}

	@Transactional
	@AmqpEventListener
	public void onDeleteQuotaEvent(DeleteQuotaEvent event) {
		if (event == null) {
			throw new MessageRejectedException("DeleteQuotaEvent war null - ungültiges Event");
		}

		logger.info("Starte vollständige Logistik-Löschung für ArticleID {}", event.articleId());

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

		// Nach Commit benachrichtigen – erst dann ist die DB konsistent sichtbar
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				badgeNotifier.notifyNow();
			}
		});
	}

	private void freeStorageLocations(Long articleId) {
		ArticleInfo articleInfo = articleInfoRepo.findByArticleId(articleId);

		if (articleInfo == null) {
			logger.info("Kein ArticleInfo-Eintrag für ArticleID {} -> keine Lagerplätze freizugeben", articleId);
			return;
		}

		int freedCount = 0;
		freedCount += freeOneLocation(articleInfo.getStorageLocation());
		freedCount += freeOneLocation(articleInfo.getReserveStorageLocation());

		logger.info("{} Lagerplätze freigegeben für ArticleID {}", freedCount, articleId);
	}

	private void deleteStocks(Long articleId) {
		long deletedCount = articleInfoRepo.deleteByArticleId(articleId);
		logger.info("{} Bestandsdatensätze gelöscht für ArticleID {}", deletedCount, articleId);
	}

	private void deleteComissionEntrys(Long articleId) {
		ArticleInfo articleInfo = articleInfoRepo.findByArticleId(articleId);

		if (articleInfo == null) {
			logger.info("Kein ArticleInfo-Eintrag für ArticleID {} -> keine MessageLogistic-Einträge löschbar", articleId);
			return;
		}

		long deletedCount = messageLogisticRepository.deleteByArticleId(articleId);
		logger.info("{} offene Filial-Bedarfe gelöscht für ArticleID {}", deletedCount, articleId);
	}

	private void deleteOrderPickingEntrys(Long articleId) {
		long deletedCount = kommissionPositionRepository.deleteByArticleId(articleId);
		logger.info("{} offene Kommissionierungen gelöscht für ArticleID {}", deletedCount, articleId);
	}

	private int freeOneLocation(String generalId) {
		if (generalId == null || generalId.isBlank()) {
			return 0;
		}

		try {
			String[] parts = generalId.split("\\.");
			String zonePart = parts[0];
			String shelfPart = parts[1];
			String compartmentPart = parts[2];

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
			logger.error("Lagerplatz '{}' konnte nicht freigegeben werden – bleibt gesperrt!", generalId, e);
			return 0;
		}
	}
}