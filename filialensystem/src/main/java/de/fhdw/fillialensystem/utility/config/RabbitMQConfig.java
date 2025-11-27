package de.fhdw.fillialensystem.utility.config;

import de.fhdw.commons.api.rabbitmq.DomainQueue;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;

    public RabbitMQConfig() {
        super();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setUseTemporaryReplyQueues(true);
        template.setReplyTimeout(5000);
        template.setMessageConverter(jackson2JsonMessageConverter);
        return template;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("*");

        DefaultJackson2JavaTypeMapper javaTypeMapper = new DefaultJackson2JavaTypeMapper();
        javaTypeMapper.setTrustedPackages("*");

        converter.setClassMapper(classMapper);
        converter.setJavaTypeMapper(javaTypeMapper);
        converter.setCreateMessageIds(true);

        return converter;
    }

    @Bean
    public DirectExchange appExchange() {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public Declarables domainDeclarables(DirectExchange appExchange) {
        List<Declarable> declarables = new ArrayList<>();

        for (DomainQueue dq : DomainQueue.values()) {
            Queue queue = new Queue(dq.getQueue(), true);
            Binding binding = BindingBuilder
                    .bind(queue)
                    .to(appExchange)
                    .with(dq.getQueue());

            declarables.add(queue);
            declarables.add(binding);
        }

        return new Declarables(declarables);
    }
}