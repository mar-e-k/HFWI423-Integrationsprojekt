package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.ContingentResponseDTO;
import fhdw.de.einkauf_service.service.ContingentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contingents")
@Tag(name = "Contingents", description = "Aktive Kontingente, die im Lager bereitstehen")
public class ContingentController {

    private final ContingentService contingentService;

    public ContingentController(ContingentService contingentService){
        this.contingentService = contingentService;
    }

    @GetMapping
    @Operation(summary = "Übersicht aller aktiven Kontingente",
            description = "Liefert alle aktuell für das Lager verfügbaren Kontingente.")
    @ApiResponse(responseCode = "200", description = "Liste der Kontingente")
    public ResponseEntity<List<ContingentResponseDTO>> getContingentOverview() {
        return ResponseEntity.ok(contingentService.getAllAvailableContingents());
    }
}
