package de.fhdw.kassensystem.rest.proxy.services;

import de.fhdw.commons.api.controller.AccountAPI;
import de.fhdw.commons.api.dto.AccountDTO;
import de.fhdw.kassensystem.utility.FilialClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountProxyService extends AbstractProxyService implements AccountAPI {

    public AccountProxyService(FilialClient filialClient) {
        super(filialClient);
    }

    @Override
    public List<AccountDTO> findAll() {
        return filialClient.getWebClient()
                .get()
                .uri("/api/account")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AccountDTO>>(){})
                .block();
    }

    @Override
    public Optional<AccountDTO> findById(Long id) {
        return Optional.ofNullable(filialClient.getWebClient()
                .get()
                .uri("/api/account/id/{id}", id)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block());
    }

    @Override
    public Optional<AccountDTO> findByUuid(String uuid) {
        return Optional.ofNullable(filialClient.getWebClient()
                .get()
                .uri("/api/account/uuid/{uuid}", uuid)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block());
    }

    @Override
    public Optional<AccountDTO> findByUsername(String username) {
        return Optional.ofNullable(filialClient.getWebClient()
                .get()
                .uri("/api/account/name/{username}", username)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block());
    }
}