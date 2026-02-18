package de.fhdw.vendix.store.utility;

import de.fhdw.vendix.commons.core.api.dto.SystemClientDTO;
import de.fhdw.vendix.store.persistence.entity.Register;

import java.time.Instant;

public final class RegisterClient {

    private SystemClientDTO systemClientDTO;
    private Register register;
    private Instant registeredAt;
    private Instant lastSeen;
    private boolean online;

    public RegisterClient() {}

    public RegisterClient(SystemClientDTO systemClientDTO, Register register, Instant registeredAt, Instant lastSeen, boolean online) {
        this.systemClientDTO = systemClientDTO;
        this.register = register;
        this.registeredAt = registeredAt;
        this.lastSeen = lastSeen;
        this.online = online;
    }

    public SystemClientDTO getSystemClientDTO() {
        return systemClientDTO;
    }

    public void setSystemClientDTO(SystemClientDTO systemClientDTO) {
        this.systemClientDTO = systemClientDTO;
    }

    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}