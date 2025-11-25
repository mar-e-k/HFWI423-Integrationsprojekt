package de.fhdw.fillialensystem.api.rabbitmq;

import de.fhdw.commons.api.dto.GenericDTO;
import de.fhdw.commons.api.rabbitmq.CommandMessage;
import de.fhdw.commons.api.rabbitmq.CommandResult;
import de.fhdw.commons.api.rabbitmq.DomainCommand;
import de.fhdw.commons.api.rabbitmq.DomainQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class CommandSender<DTO extends GenericDTO<?>> {

    private static final Logger log = LoggerFactory.getLogger(CommandSender.class);

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;

    private final RabbitTemplate rabbitTemplate;

    public CommandSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public CommandResult<DTO> send(DomainQueue queue, DomainCommand command, DTO payload) {
        try {
            if (queue == null) {
                throw new IllegalArgumentException("Cannot send message to queue. Queue is null");
            }
            if (command == null) {
                throw new IllegalArgumentException("Cannot send message to queue [%s]. Command is null".formatted(queue.name()));
            }
            if (payload == null) {
                throw new IllegalArgumentException("Cannot send message to queue [%s]. Payload is null".formatted(queue.name()));
            }

            MessagePostProcessor mpp = m -> {
                m.getMessageProperties().setHeader("__TypeId__", CommandMessage.class.getName());
                m.getMessageProperties().setHeader("__ContentTypeId__", payload.getClass().getName());
                return m;
            };

            CommandResult<DTO> reply = rabbitTemplate.convertSendAndReceiveAsType(
                    exchange,
                    queue.getQueue(),
                    CommandMessage.create(command, payload),
                    mpp,
                    ParameterizedTypeReference.forType(CommandResult.class)
            );

            if (reply == null) {
                throw new IllegalStateException("Could not send command [%s] to queue [%s]. Queue message returned null".formatted(command, queue.name()));
            }

            return reply;
        } catch (Exception e) {
            log.atError().log(e.getMessage(), e.getCause());
            throw e;
        }
    }
}