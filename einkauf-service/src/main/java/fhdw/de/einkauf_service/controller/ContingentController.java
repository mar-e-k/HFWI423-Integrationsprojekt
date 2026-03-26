package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.ContingentResponseDTO;
import fhdw.de.einkauf_service.service.ContingentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contingents")
public class ContingentController {

    private final ContingentService contingentService;

    public ContingentController(ContingentService contingentService){
        this.contingentService = contingentService;
    }

    /**
     * Ruft die Übersicht aller aktiven Kontingente für das Lager ab.
     */
    @GetMapping
    public ResponseEntity<List<ContingentResponseDTO>> getContingentOverview() {
        List<ContingentResponseDTO> response = contingentService.getAllAvailableContingents();
        return ResponseEntity.ok(response);
    }
}
