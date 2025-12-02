package de.fhdw.kassensystem.utility;

import de.fhdw.commons.api.dto.StoreDTO;
import de.fhdw.commons.utility.AuthContext;
import de.fhdw.kassensystem.utility.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class StoreClient {

    private StoreDTO storeDTO;

    private final WebClient webClient;

    public StoreClient(@Value("${server.filialsystem.uri}") String uri, @Lazy JwtService jwtService) {
        this.webClient = WebClient.builder()
                .baseUrl(uri)
                .filter((request, next) -> {
                    Object token;
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        token = jwtService.generateSystemToken();
                    } else {
                        token = jwtService.generateToken(jwtService.getCurrentAuth()
                                .orElseThrow(IllegalStateException::new));
                    }
                    ClientRequest newRequest = ClientRequest.from(request)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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