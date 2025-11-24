package de.fhdw.commons.rest.rabbitmq;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.fhdw.commons.rest.dto.GenericDTO;

public class CommandResult<DTO extends GenericDTO<?>> {

    private boolean success;
    private DomainCommand command;

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = JsonTypeInfo.As.PROPERTY,
            property = "@class"
    )
    private DTO payload;
    private String error;

    public CommandResult() {
        super();
    }

    public CommandResult(boolean success, DomainCommand command, DTO payload, String error) {
        this.success = success;
        this.command = command;
        this.payload = payload;
        this.error = error;
    }

    public static <DTO extends GenericDTO<?>> CommandResult<DTO> success(DomainCommand command, DTO payload) {
        return new CommandResult<>(true, command, payload, null);
    }

    public static <DTO extends GenericDTO<?>> CommandResult<DTO> failure(DomainCommand command, String error) {
        return new CommandResult<>(false, command, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public DomainCommand getCommand() {
        return command;
    }

    public void setCommand(DomainCommand command) {
        this.command = command;
    }

    public DTO getPayload() {
        return payload;
    }

    public void setPayload(DTO payload) {
        this.payload = payload;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}