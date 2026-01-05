package de.fhdw.commons.api.rabbitmq;

import de.fhdw.commons.api.dto.GenericDTO;

public record CommandMessage<DTO extends GenericDTO<?>>(
        DomainCommand command,
        DTO payload
) {
    public CommandMessage {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }
    }

    public static <DTO extends GenericDTO<?>> CommandMessage<DTO> create(DomainCommand command, DTO payload) {
        return new CommandMessage<>(command, payload);
    }
}