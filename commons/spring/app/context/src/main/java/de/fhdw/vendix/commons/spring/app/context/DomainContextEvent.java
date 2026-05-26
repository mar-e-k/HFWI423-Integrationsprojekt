package de.fhdw.vendix.commons.spring.app.context;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.springframework.context.ApplicationEvent;

public abstract class DomainContextEvent extends ApplicationEvent {

    private final DomainDTO domain;

    public DomainContextEvent(Object source, DomainDTO domain) {
        super(source);
        this.domain = domain;
    }

    public DomainDTO getDomain() {
        return domain;
    }
}