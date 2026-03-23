package de.fhdw.vendix.pos.core.persistence;

import org.springframework.stereotype.Service;

@Service
public class AccountAdapter {

//    public Optional<AccountDTO> findById(Long id) {
//        return getWebClient()
//                .get()
//                .uri("/api/cashier/id/{id}", id)
//                .retrieve()
//                .bodyToMono(AccountDTO.class)
//                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
//                .blockOptional();
//    }
//
//    public Optional<AccountDTO> findByUuid(String uuid) {
//        return getWebClient()
//                .get()
//                .uri("/api/cashier/subject/{subject}", uuid)
//                .retrieve()
//                .bodyToMono(AccountDTO.class)
//                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
//                .blockOptional();
//    }
//
//    public Optional<AccountDTO> findByUsername(String username) {
//        return getWebClient()
//                .get()
//                .uri("/api/cashier/name/{accountUsername}", username)
//                .retrieve()
//                .bodyToMono(AccountDTO.class)
//                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
//                .blockOptional();
//    }
}