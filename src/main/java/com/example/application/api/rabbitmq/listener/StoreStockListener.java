package com.example.application.api.rabbitmq.listener;

import com.example.application.api.rabbitmq.producer.DomainQueue;
import com.example.application.data.orderPicking.MessageLogistic;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import de.fhdw.commons.api.dto.LogisticMessageDTO;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import jakarta.annotation.PostConstruct;

@Component
public class StoreStockListener {

    private final RabbitTemplate rabbitTemplate;
    private final MessageLogisticRepository msgRepo;

    // Konstruktor-Injektion für Spring
    public StoreStockListener(RabbitTemplate rabbitTemplate, MessageLogisticRepository msgRepo) {
        this.rabbitTemplate = rabbitTemplate;
        this.msgRepo = msgRepo;
    }

    @RabbitListener(queues = "#{T(com.example.application.api.rabbitmq.producer.DomainQueue).LOGISTIC_STORE_RESTOCK.getQueue()}")
    public void handleStock(LogisticMessageDTO msg) {
        System.out.println("Instance:" + msg.toString());
        System.out.println("StoreId:" + msg.getStoreId());
        System.out.println("ArticleId:" + msg.getArticleId());
        System.out.println("Quantity:" + msg.getQuantity());

        // In die DB speichern
        MessageLogistic ml = new MessageLogistic();
        ml.setStoreId(String.valueOf(msg.getStoreId()));
        ml.setArticleNumber(String.valueOf( msg.getArticleId()));
        ml.setQuantity(msg.getQuantity());

        msgRepo.save(ml);
        System.out.println("MessageLogistic in DB gespeichert: " + ml);
    }

    @PostConstruct
    public void sendTestMessage() {
        LogisticMessageDTO msg = new LogisticMessageDTO();
        msg.setStoreId(01L);
        msg.setArticleId(13L);
        msg.setQuantity(5L);

        rabbitTemplate.convertAndSend(DomainQueue.LOGISTIC_STORE_RESTOCK.getQueue(), msg);
        System.out.println("Testnachricht gesendet!");
    }
}
