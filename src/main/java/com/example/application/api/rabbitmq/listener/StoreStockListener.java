package com.example.application.api.rabbitmq.listener;

import de.fhdw.commons.api.dto.LogisticMessageDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StoreStockListener {

    @RabbitListener(queues = "#{T(com.example.application.api.rabbitmq.producer.DomainQueue).LOGISTIC_STORE_RESTOCK.queue}")
    public void handleStock(LogisticMessageDTO commandMessage) {
        System.out.println("Instance:" + commandMessage.toString());
        System.out.println("StoreId:" + commandMessage.getStoreId());
        System.out.println("ArticleId:" + commandMessage.getArticleId());
        System.out.println("Quantity:" + commandMessage.getQuantity());
    }
}