package de.fhdw.vendix.commons.spring.security;

import java.util.Optional;
import java.util.UUID;

public interface SecurityService {
    Optional<UUID> getAuthenticatedUserUuid();
}