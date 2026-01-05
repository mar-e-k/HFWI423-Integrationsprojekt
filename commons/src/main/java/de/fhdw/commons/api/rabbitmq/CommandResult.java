package de.fhdw.commons.api.rabbitmq;

import de.fhdw.commons.api.dto.GenericDTO;

public record CommandResult<DTO extends GenericDTO<?>>(
        boolean success,
        DomainCommand command,
        DTO payload,
        String error
) {
    public CommandResult {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }
        if (error == null || error.isBlank()) {
            throw new IllegalArgumentException("Error message cannot be null or blank");
        }
    }

    public static <DTO extends GenericDTO<?>> CommandResult<DTO> success(DomainCommand command, DTO payload) {
        return new CommandResult<>(true, command, payload, null);
    }

    public static <DTO extends GenericDTO<?>> CommandResult<DTO> failure(DomainCommand command, String error) {
        return new CommandResult<>(false, command, null, error);
    }
}