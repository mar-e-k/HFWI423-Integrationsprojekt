package com.example.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import io.github.plaguv.core.publisher.EventPublisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The entry point of the Spring Boot application.
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@EnableScheduling
@SpringBootApplication
@Theme(value = "my-app")
public class Application implements AppShellConfigurator {

private final EventPublisher publisher;

    public Application(EventPublisher publisher) {
        this.publisher = publisher;
    }


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void configurePage(AppShellSettings settings) {
        // Pfad relativ zum Kontext, entspricht src/main/resources/META-INF/resources/icons/icon.png
        settings.addFavIcon("icon", "icons/icon.png", "32x32");
        settings.addLink("shortcut icon", "icons/icon.png");
    }
    @EventListener
    public void message(ApplicationStartedEvent event){

    }
}
