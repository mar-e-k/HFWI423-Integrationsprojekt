package com.example.application.api.rabbitmq.listener;

import com.example.application.api.dto.LogisticMessageDTO;
import com.example.application.api.rabbitmq.producer.CommandMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StoreStockListener {

    @RabbitListener(queues = "#{T(com.example.application.api.rabbitmq.producer.DomainQueue).LOGISTIC_STORE_RESTOCK.queue}")
    public void handleStock(CommandMessage<LogisticMessageDTO> commandMessage) {
        System.out.println("Instance:" + commandMessage.toString());
        System.out.println("StoreId:" + commandMessage.getPayload().getStoreId());
        System.out.println("ArticleId:" + commandMessage.getPayload().getArticleId());
        System.out.println("Quantity:" + commandMessage.getPayload().getQuantity());
    }
}