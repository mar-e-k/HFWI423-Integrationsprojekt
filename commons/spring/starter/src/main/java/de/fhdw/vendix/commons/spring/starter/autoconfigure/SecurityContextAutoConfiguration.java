package de.fhdw.vendix.commons.spring.starter.autoconfigure;

import de.fhdw.vendix.commons.security.core.context.DefaultRegisterContext;
import de.fhdw.vendix.commons.security.core.context.DefaultStoreContext;
import de.fhdw.vendix.commons.security.spring.context.DefaultAppContext;
import de.fhdw.vendix.security.api.context.AppContext;
import de.fhdw.vendix.security.api.context.RegisterContext;
import de.fhdw.vendix.security.api.context.StoreContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    public StoreContext storeContext() {
        return new DefaultStoreContext();
    }

    @Bean
    @ConditionalOnMissingBean
    public RegisterContext registerContext() {
        return new DefaultRegisterContext();
    }
}