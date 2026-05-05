package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.spring.web.api.orchestrator.RegisterApi;
import de.fhdw.vendix.orchestrator.core.domain.register.RegisterMapper;
import de.fhdw.vendix.orchestrator.core.domain.register.RegisterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
class RegisterController implements RegisterApi {

    private final RegisterService registerService;
    private final RegisterMapper registerMapper;

    RegisterController(RegisterService registerService, RegisterMapper registerMapper) {
        this.registerService = registerService;
        this.registerMapper = registerMapper;
    }

    @Override
    public ResponseEntity<RegisterDTO> getRegisterById(Long id) {
        return registerService;
    }
}
