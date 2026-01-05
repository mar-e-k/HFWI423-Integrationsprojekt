package de.fhdw.kassensystem.utility;

import de.fhdw.commons.api.dto.StoreDTO;
import de.fhdw.commons.security.utility.security.auth.AuthContextHolder;
import de.fhdw.commons.security.utility.security.jwt.JwtService;
import de.fhdw.commons.security.utility.security.jwt.claims.JwtPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public final class StoreClient {

    private StoreDTO storeDTO;

    private final WebClient webClient;

    public StoreClient(@Value("${server.filialsystem.uri}") String uri, JwtService jwtService) {
        this.webClient = WebClient.builder()
                .baseUrl(uri)
                .filter((request, next) -> {
                    Object token;
                    if (AuthContextHolder.current().isEmpty()) {
                        token = jwtService.generateToken(JwtPayload.system());
                    } else {
                        token = jwtService.generateToken(JwtPayload.user());
                    }
                    ClientRequest newRequest = ClientRequest.from(request)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(token))
                            .build();

                    return next.exchange(newRequest);
                })
                .build();
    }

    public StoreDTO getStoreDTO() {
        return storeDTO;
    }

    public void setStoreDTO(StoreDTO storeDTO) {
        this.storeDTO = storeDTO;
    }

    public WebClient getWebClient() {
        return webClient;
    }
}