package com.example.application.api.rabbitmq.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CommandSender<DTO> {

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;

    private final RabbitTemplate rabbitTemplate;

    public CommandSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void fire(DomainQueue queue, DTO payload) {
        try {
            if (queue == null) {
                throw new IllegalArgumentException("Cannot send message to queue. Queue is null");
            }
            if (payload == null) {
                throw new IllegalArgumentException("Cannot send message to queue [%s]. Payload is null".formatted(queue.name()));
            }

            rabbitTemplate.convertAndSend(
                    exchange,
                    queue.getQueue(),
                    payload);
        } catch (Exception e) {
            throw e;
        }
    }
}
