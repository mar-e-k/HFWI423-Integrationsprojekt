package com.example.application.amqp;

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Workaround fuer Spring AMQP 4.0:
 * SimpleAmqpHeaderMapper.toHeaders() unboxt priority ohne Null-Check → NPE.
 *
 * Zwei Absicherungen:
 * 1. @Bean rabbitListenerContainerFactory – ueberschreibt Spring Boots Default-Factory
 * 2. BeanPostProcessor – greift zusaetzlich auf alle Container-Factories/-Container
 *    der plaguv-Library (oder anderer Auto-Configs) zu, unabhaengig vom Bean-Namen.
 *    defaultRequeueRejected(false) verhindert die Endlosschleife bei Conversion-Fehlern.
 */
@Configuration
public class RabbitConfig implements BeanPostProcessor {

    private static final MessagePostProcessor PRIORITY_FIX = message -> {
        if (message.getMessageProperties().getPriority() == null) {
            message.getMessageProperties().setPriority(0);
        }
        return message;
    };

    /** Ueberschreibt Spring Boots auto-konfigurierte Default-Factory. */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAfterReceivePostProcessors(PRIORITY_FIX);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    /**
     * Greift auf ALLE SimpleRabbitListenerContainerFactory-Beans ein,
     * also auch die der plaguv-Library, egal unter welchem Bean-Namen.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        if (bean instanceof SimpleRabbitListenerContainerFactory factory) {
            factory.setAfterReceivePostProcessors(PRIORITY_FIX);
            factory.setDefaultRequeueRejected(false);
        }
        if (bean instanceof SimpleMessageListenerContainer container) {
            container.setAfterReceivePostProcessors(PRIORITY_FIX);
            container.setDefaultRequeueRejected(false);
        }
        return bean;
    }
}
