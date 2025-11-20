package de.fhdw.kassensystem.rest.rabbitmq;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.fhdw.kassensystem.rest.dto.GenericDTO;

public class CommandMessage<DTO extends GenericDTO<?>> {

    private DomainCommand command;

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = JsonTypeInfo.As.PROPERTY,
            property = "@class"
    )
    private DTO payload;

    public CommandMessage() {
        super();
    }

    public CommandMessage(DomainCommand command, DTO payload) {
        this.command = command;
        this.payload = payload;
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
}