package de.fhdw.kassensystem.utility.shutdown;

import de.fhdw.kassensystem.persistance.service.proxy.AccountProxyService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class AccountCleanupShutdown implements ApplicationListener<ContextClosedEvent> {

    private static final Logger log = LoggerFactory.getLogger(AccountCleanupShutdown.class);

    private final AccountProxyService accountProxyService;

    public AccountCleanupShutdown(AccountProxyService accountProxyService) {
        this.accountProxyService = accountProxyService;
    }

    @Override
    public void onApplicationEvent(@NotNull ContextClosedEvent event) {


    }
}