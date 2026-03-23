package de.fhdw.vendix.commons.api.structure.web;

import java.util.Set;

public interface WebEndpoint {
    String getBasePath();

    Set<String> getEndpoints();
}