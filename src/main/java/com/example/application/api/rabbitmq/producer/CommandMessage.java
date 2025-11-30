package com.example.application.api.rabbitmq.producer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class CommandMessage<DTO> {

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

    public static <DTO> CommandMessage<DTO> create(DomainCommand command, DTO payload) {
        return new CommandMessage<>(command, payload);
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