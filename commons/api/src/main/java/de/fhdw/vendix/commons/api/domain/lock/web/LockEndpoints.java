package de.fhdw.vendix.commons.api.domain.lock.web;

import de.fhdw.vendix.commons.api.structure.web.WebEndpoint;

import java.util.Set;

public final class LockEndpoints implements WebEndpoint {

    public static final String BASE = "/api/lock";

    public static final String BY_TARGET_TYPE_AND_TARGET_ID = BASE + "/type/{targetType}/id/{targetId}";
    public static final String BY_INSTANCE_UUID = BASE + "/instance/{instanceUUID}";

    private LockEndpoints() {}

    @Override
    public String getBasePath() {
        return BASE;
    }

    @Override
    public Set<String> getEndpoints() {
        return Set.of(
                BY_TARGET_TYPE_AND_TARGET_ID,
                BY_INSTANCE_UUID
        );
    }
}