package com.example.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI logistikOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Logistik-Service API")
                        .description("REST-Schnittstelle des Logistik-Services. Erreichbar via API-Gateway auf Port 8080.")
                        .version("1.0.0"));
    }
}
