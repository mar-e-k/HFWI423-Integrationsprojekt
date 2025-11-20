package de.fhdw.kassensystem.rest.rabbitmq;

import de.fhdw.kassensystem.rest.dto.GenericDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CommandSender<DTO extends GenericDTO<?>> {

    private static final Logger log = LoggerFactory.getLogger(CommandSender.class);

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;

    private final RabbitTemplate rabbitTemplate;


    public CommandSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @SuppressWarnings("unchecked")
    public CommandResult<DTO> send(DomainQueue queue, DomainCommand command, DTO payload) {
        try {
            if (Objects.isNull(queue)) {
                throw new IllegalArgumentException("Cannot send message. Queue is null");
            }
            if (Objects.isNull(command)) {
                throw new IllegalArgumentException("Cannot send message. Command is null");
            }
            if (Objects.isNull(payload)) {
                throw new IllegalArgumentException("Cannot send message. Payload is null");
            }

            MessagePostProcessor mpp = m -> {
                m.getMessageProperties().setHeader("__TypeId__", CommandMessage.class.getName());
                m.getMessageProperties().setHeader("__ContentTypeId__", payload.getClass().getName());
                return m;
            };

            Object reply = rabbitTemplate.convertSendAndReceive(
                    exchange,
                    queue.getQueue(),
                    new CommandMessage<>(command, payload),
                    mpp
            );

            if (Objects.isNull(reply)) {
                throw new IllegalStateException(
                        String.format("Could not send command [%s] to queue [%s]", command, queue.name())
                );
            }

            return (CommandResult<DTO>) reply;
        } catch (Exception e) {
            log.atError().log(e.getMessage(), e.getCause());
            throw e;
        }
    }
}