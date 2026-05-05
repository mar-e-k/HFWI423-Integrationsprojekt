package de.fhdw.vendix.commons.spring.starter.autoconfigure.app;


import de.fhdw.vendix.commons.spring.app.context.app.AppContext;
import de.fhdw.vendix.commons.spring.app.context.app.DefaultAppContext;
import de.fhdw.vendix.commons.spring.app.context.register.DefaultRegisterContext;
import de.fhdw.vendix.commons.spring.app.context.register.RegisterContext;
import de.fhdw.vendix.commons.spring.app.context.store.DefaultStoreContext;
import de.fhdw.vendix.commons.spring.app.context.store.StoreContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
public class ContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AppContext appContext(Environment environment, ApplicationEventPublisher publisher) {
        return new DefaultAppContext(environment, publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public RegisterContext registerContext(AppContext appContext, ApplicationEventPublisher publisher) {
        return new DefaultRegisterContext(appContext, publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public StoreContext storeContext(AppContext appContext, ApplicationEventPublisher publisher) {
        return new DefaultStoreContext(appContext, publisher);
    }
}