package de.fhdw.vendix.commons.spring.starter.autoconfigure.security;

import de.fhdw.vendix.commons.spring.app.context.app.AppContext;
import de.fhdw.vendix.commons.spring.app.lifecycle.login.DefaultLoginHandler;
import de.fhdw.vendix.commons.spring.app.lifecycle.login.LoginHandler;
import de.fhdw.vendix.commons.spring.app.lifecycle.logout.DefaultLogoutHandler;
import de.fhdw.vendix.commons.spring.app.lifecycle.logout.LogoutHandler;
import de.fhdw.vendix.commons.spring.app.lifecycle.shutdown.DefaultShutdownHandler;
import de.fhdw.vendix.commons.spring.app.lifecycle.shutdown.ShutdownHandler;
import de.fhdw.vendix.commons.spring.security.DefaultSecurityService;
import de.fhdw.vendix.commons.spring.security.SecurityService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.ConnectionProxyService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.DistributedLockProxyService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = SecurityLifecycleAutoConfiguration.class)
public class SecurityContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SecurityService securityService() {
        return new DefaultSecurityService();
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