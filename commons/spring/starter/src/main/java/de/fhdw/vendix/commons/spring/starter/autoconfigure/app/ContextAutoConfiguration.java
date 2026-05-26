package de.fhdw.vendix.commons.spring.starter.autoconfigure.app;


import de.fhdw.vendix.commons.spring.app.context.register.DefaultRegisterContext;
import de.fhdw.vendix.commons.spring.app.context.register.RegisterContext;
import de.fhdw.vendix.commons.spring.app.context.store.DefaultStoreContext;
import de.fhdw.vendix.commons.spring.app.context.store.StoreContext;
import de.fhdw.vendix.commons.spring.app.context.system.DefaultSystemContext;
import de.fhdw.vendix.commons.spring.app.context.system.SystemContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
public class ContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SystemContext systemContext(Environment environment, ApplicationEventPublisher publisher) {
        return new DefaultSystemContext(environment, publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public RegisterContext registerContext(SystemContext systemContext, ApplicationEventPublisher publisher) {
        return new DefaultRegisterContext(systemContext, publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public StoreContext storeContext(SystemContext systemContext, ApplicationEventPublisher publisher) {
        return new DefaultStoreContext(systemContext, publisher);
    }
}