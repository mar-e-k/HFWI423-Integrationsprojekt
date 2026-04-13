package de.fhdw.vendix.commons.spring.starter.autoconfigure.security;

import de.fhdw.vendix.commons.spring.app.context.app.AppContext;
import de.fhdw.vendix.commons.spring.app.context.app.DefaultAppContext;
import de.fhdw.vendix.commons.spring.app.context.register.DefaultRegisterContext;
import de.fhdw.vendix.commons.spring.app.context.register.RegisterContext;
import de.fhdw.vendix.commons.spring.app.context.store.DefaultStoreContext;
import de.fhdw.vendix.commons.spring.app.context.store.StoreContext;
import de.fhdw.vendix.commons.spring.app.lifecycle.context.ContextInitializationDelegator;
import de.fhdw.vendix.commons.spring.app.lifecycle.context.DefaultContextInitializationDelegator;
import de.fhdw.vendix.commons.spring.app.lifecycle.login.DefaultLoginHandler;
import de.fhdw.vendix.commons.spring.app.lifecycle.login.LoginHandler;
import de.fhdw.vendix.commons.spring.app.lifecycle.logout.DefaultLogoutHandler;
import de.fhdw.vendix.commons.spring.app.lifecycle.logout.LogoutHandler;
import de.fhdw.vendix.commons.spring.app.lifecycle.shutdown.DefaultShutdownHandler;
import de.fhdw.vendix.commons.spring.app.lifecycle.shutdown.ShutdownHandler;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.ConnectionProxyService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.DistributedLockProxyService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
public class SecurityContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AppContext appContext(Environment environment) {
        return new DefaultAppContext(environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public StoreContext storeContext(AppContext appContext, ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultStoreContext(appContext, applicationEventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public RegisterContext registerContext(AppContext appContext, ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultRegisterContext(appContext, applicationEventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public ContextInitializationDelegator contextInitializationDelegator(AppContext appContext, ConnectionProxyService connectionProxyService, DistributedLockProxyService distributedLockProxyService) {
        return new DefaultContextInitializationDelegator(appContext, connectionProxyService, distributedLockProxyService);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoginHandler loginHandler(AppContext appContext, DistributedLockProxyService distributedLockProxyService) {
        return new DefaultLoginHandler(appContext, distributedLockProxyService);
    }

    @Bean
    @ConditionalOnMissingBean
    public LogoutHandler logoutHandler(DistributedLockProxyService distributedLockProxyService) {
        return new DefaultLogoutHandler(distributedLockProxyService);
    }

    @Bean
    @ConditionalOnMissingBean
    public ShutdownHandler shutdownHandler(AppContext appContext, DistributedLockProxyService distributedLockProxyService, ConnectionProxyService connectionProxyService) {
        return new DefaultShutdownHandler(appContext, distributedLockProxyService, connectionProxyService);
    }
}