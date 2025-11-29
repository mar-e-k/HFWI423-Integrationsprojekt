package de.fhdw.fillialensystem.utility;

import de.fhdw.commons.api.dto.SystemClientDTO;

import java.time.Instant;


public class RegisterClient {

    private SystemClientDTO systemClientDTO;
    private Instant registeredAt;
    private Instant lastSeen;
    private boolean online;

    public RegisterClient() {
        super();
    }

    public RegisterClient(SystemClientDTO systemClientDTO, Instant registeredAt, Instant lastSeen, boolean online) {
        this.systemClientDTO = systemClientDTO;
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