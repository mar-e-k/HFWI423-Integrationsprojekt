package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.system.SystemEndpoints;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(SystemEndpoints.BASE)
@Tag(name = "Kassensystem Registry", description = "Endpoints for operations related to the registry of a kassensystem")
public class RegisterRegistryController {

    // TODO

//    private final RegisterRegistryService registerRegistryService;1
//
//    public RegisterRegistryController(RegisterRegistryService registerRegistryService, RegisterMapper registerMapper) {
//        this.registerRegistryService = registerRegistryService;
//        this.registerMapper = registerMapper;
//    }
//
//    @PostMapping
//    @Operation(summary = "Register a kassensystem")
//    public ResponseEntity<RegisterDTO> registerKassensystem(@RequestBody SystemDTO dto) {
//        registerRegistryService.addRegistry(dto);
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(registerRegistryService.findRegistryById(systemClientDTO.getId())
//                        .map(RegisterClient::getRegister)
//                        .map(registerMapper::toDto)
//                        .orElseThrow(EntityNotFoundException::new));
//    }
//
//    @DeleteMapping("/{id}")
//    @Operation(summary = "Deregister a kassensystem")
//    public ResponseEntity<Void> deregisterKassensystem(@PathVariable String id) {
//        registerRegistryService.deleteRegistry(id);
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .build();
//    }
}
