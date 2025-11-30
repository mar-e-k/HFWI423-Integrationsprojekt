package de.fhdw.kassensystem.utility;

import com.vaadin.flow.server.VaadinSession;
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
                    Object token;
                    if (VaadinSession.getCurrent() == null || VaadinSession.getCurrent().getAttribute("jwt") == null) {
                        token = jwtService.generateSystemToken();
                    } else {
                        token = VaadinSession.getCurrent().getAttribute("jwt");
                    }

                    ClientRequest newRequest = ClientRequest.from(request)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .build();

                    return next.exchange(newRequest);
                })
                .build();
    }

    public WebClient getWebClient() {
        return webClient;
    }
}