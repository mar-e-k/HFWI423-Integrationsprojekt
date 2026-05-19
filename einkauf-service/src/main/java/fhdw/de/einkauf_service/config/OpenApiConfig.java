package fhdw.de.einkauf_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI einkaufServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Einkauf Service API")
                        .version("v1")
                        .description("REST-API des Einkauf-Service. Verträge sind versioniert unter `/api/v1/...`. "
                                + "Fehler werden als RFC 7807 ProblemDetail (application/problem+json) ausgeliefert. "
                                + "Asynchrone Integration mit anderen Services erfolgt über AMQP-Events (RabbitMQ).")
                        .contact(new Contact()
                                .name("FHDW Integrationsprojekt HFWI423")
                                .email("digitalmarketing@myvi.de"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Lokale Entwicklung"),
                        new Server().url("/").description("Aktueller Host")
                ));
    }
}
