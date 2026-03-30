package de.fhdw.vendix.pos.app.config;

import de.fhdw.vendix.security.api.jwt.JwtService;
import org.openapitools.configuration.HttpInterfacesAbstractConfigurator;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpConfig extends HttpInterfacesAbstractConfigurator {

    public HttpConfig(JwtService jwtService) {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .defaultHeaders(headers -> headers.setBearerAuth(jwtService.generateToken()))
                .build();
        super(restClient);
    }
}