package de.fhdw.vendix.store.application.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

//TODO: rewrite in new listener

@Service
public class LogisticReceiveStockListener {

    private static final Logger log = LoggerFactory.getLogger(LogisticReceiveStockListener.class);

    public LogisticReceiveStockListener() {
//        this.storeClient = storeClient;
//        this.articleService = articleService;
//        this.storeLinkStockService = storeLinkStockService;
    }

    // TODO: replace Object with actual DOT

//    @AmqpListener
//    public void receiveStock(Object event) {
    // TODO: null validation might be QOL

//        if (event == null) {
//            log.atError().log("Received null event");
//        }
//    }

//    @RabbitListener(queues = "#{T(de.fhdw.commons.api.rabbitmq.DomainQueue).STORE_LOGISTIC_RESTOCK.getQueue()}")
//    public void handleStock(LogisticMessageDTO logisticMessageDTO) {
//        log.atInfo().log("[MESSAGE] Processing LogisticMessageDTO from queue [%s]...".formatted(DomainQueue.STORE_LOGISTIC_RESTOCK.getQueue()));
//        try {
//            validateDTO(logisticMessageDTO);
//
//            if (storeClient.getStore() == null) {
//                throw new IllegalStateException("Store not initialized yet");
//            }
//
//            if (!storeClient.getStore().getId().equals(logisticMessageDTO.getStoreId())) {
//                return;
//            }
//
//            Article article = articleService.findById(logisticMessageDTO.getArticleId())
//                    .orElseThrow(EntityNotFoundException::new);
//            StoreLinkStock storeLinkStock = storeLinkStockService.findByStoreAndArticle(storeClient.getStore(), article)
//                    .orElseThrow(EntityNotFoundException::new);
//            storeLinkStock.setAmount(storeLinkStock.getAmount() + logisticMessageDTO.getQuantity().intValue());
//
//            storeLinkStockService.update(storeLinkStock);
//        } catch (Exception e) {
//            log.atError().log("[MESSAGE] Error while processing LogisticMessageDTO from queue [%s]. Error: [%s]".formatted(DomainQueue.STORE_LOGISTIC_RESTOCK.getQueue(), e.getMessage()));
//        }
//        log.atInfo().log("[MESSAGE] Processed a LogisticMessageDTO from queue [%s]".formatted(DomainQueue.STORE_LOGISTIC_RESTOCK.getQueue()));
//    }
}