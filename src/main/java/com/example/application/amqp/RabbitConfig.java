package com.example.application.amqp;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Workaround fuer Spring AMQP 4.0: SimpleAmqpHeaderMapper.toHeaders() unboxt
 * MessageProperties.getPriority() ohne Null-Check → NPE wenn Priority nicht gesetzt.
 * Der AfterReceivePostProcessor setzt Priority auf 0, bevor der Header-Mapper greift.
 */
@Configuration
public class RabbitConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAfterReceivePostProcessors(message -> {
            if (message.getMessageProperties().getPriority() == null) {
                message.getMessageProperties().setPriority(0);
            }
            return message;
        });
        return factory;
    }
}
