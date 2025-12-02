package de.fhdw.kassensystem.persistance.service.proxy;

import de.fhdw.commons.api.dto.AccountDTO;
import de.fhdw.kassensystem.utility.StoreClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class AccountProxyService extends AbstractProxyService {

    public AccountProxyService(StoreClient storeClient) {
        super(storeClient);
    }

    public List<AccountDTO> findAll() {
        return storeClient.getWebClient()
                .get()
                .uri("/api/account")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AccountDTO>>(){})
                .block();
    }

    public Optional<AccountDTO> findById(Long id) {
        return storeClient.getWebClient()
                .get()
                .uri("/api/account/id/{id}", id)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .blockOptional();
    }

    public Optional<AccountDTO> findByUuid(String uuid) {
        return storeClient.getWebClient()
                .get()
                .uri("/api/account/uuid/{uuid}", uuid)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .blockOptional();
    }

    public Optional<AccountDTO> findByUsername(String username) {
        return storeClient.getWebClient()
                .get()
                .uri("/api/account/name/{username}", username)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .blockOptional();
    }

    public void lockByAccount(AccountDTO accountDTO) {
        storeClient.getWebClient()
                .post()
                .uri("/api/account/lock", accountDTO)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .block();
    }

    public void deleteLockByAccount(AccountDTO accountDTO) {
        storeClient.getWebClient()
                .delete()
                .uri("/api/account/lock", accountDTO)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .block();
    }
}