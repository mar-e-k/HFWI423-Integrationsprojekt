package de.fhdw.kassensystem.utility;

import de.fhdw.kassensystem.utility.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class StoreClient {

    private final WebClient webClient;

    public StoreClient(@Value("${server.filialsystem.uri}") String uri, JwtService jwtService) {
        this.webClient = WebClient.builder()
                .baseUrl(uri)
                .filter((request, next) -> {
                    ClientRequest newRequest = ClientRequest.from(request)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(jwtService.generateToken()))
                            .build();
                    return next.exchange(newRequest);
                })
                .build();
    }

    public WebClient getWebClient() {
        return webClient;
    }
}