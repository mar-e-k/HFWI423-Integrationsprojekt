package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.ShelfPlacementRequestDTO;
import fhdw.de.einkauf_service.dto.ShelfPlacementResponseDTO;
import fhdw.de.einkauf_service.service.ShelfPlacementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shelf-placements")
@Tag(name = "Shelf Placements", description = "Platzierung von Artikeln auf Regal-Ebenen")
public class ShelfPlacementController {

    private final ShelfPlacementService placementService;

    public ShelfPlacementController(ShelfPlacementService placementService) {
        this.placementService = placementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Neue Artikel-Platzierung anlegen")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Platzierung angelegt"),
            @ApiResponse(responseCode = "400", description = "Außerhalb der Regalgrenzen (ProblemDetail)"),
            @ApiResponse(responseCode = "404", description = "Artikel oder Ebene nicht gefunden (ProblemDetail)"),
            @ApiResponse(responseCode = "409", description = "Überschneidet sich mit bestehender Platzierung (ProblemDetail)")
    })
    public ShelfPlacementResponseDTO createPlacement(@Valid @RequestBody ShelfPlacementRequestDTO placementRequestDTO) {
        return placementService.createPlacement(placementRequestDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Platzierung per ID abrufen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platzierung gefunden"),
            @ApiResponse(responseCode = "404", description = "Platzierung nicht gefunden (ProblemDetail)")
    })
    public ResponseEntity<ShelfPlacementResponseDTO> getPlacementById(@PathVariable Long id) {
        return ResponseEntity.ok(placementService.getPlacement(id));
    }

    @GetMapping("/shelf-level/{shelfLevelId}")
    @Operation(summary = "Platzierungen einer Regal-Ebene auflisten")
    public ResponseEntity<List<ShelfPlacementResponseDTO>> getPlacementsByShelfLevel(@PathVariable Long shelfLevelId) {
        return ResponseEntity.ok(placementService.getPlacementsByShelfLevel(shelfLevelId));
    }

    @GetMapping("/article/{articleId}")
    @Operation(summary = "Platzierungen eines Artikels auflisten")
    public ResponseEntity<List<ShelfPlacementResponseDTO>> getPlacementsByArticle(@PathVariable Long articleId) {
        return ResponseEntity.ok(placementService.getPlacementsByArticle(articleId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Platzierung aktualisieren")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platzierung aktualisiert"),
            @ApiResponse(responseCode = "400", description = "Außerhalb der Regalgrenzen (ProblemDetail)"),
            @ApiResponse(responseCode = "404", description = "Platzierung nicht gefunden (ProblemDetail)"),
            @ApiResponse(responseCode = "409", description = "Überschneidet sich mit bestehender Platzierung (ProblemDetail)")
    })
    public ResponseEntity<ShelfPlacementResponseDTO> updatePlacement(@PathVariable Long id,
                                                                      @Valid @RequestBody ShelfPlacementRequestDTO placementRequestDTO) {
        return ResponseEntity.ok(placementService.updatePlacement(id, placementRequestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Platzierung löschen")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Platzierung gelöscht"),
            @ApiResponse(responseCode = "404", description = "Platzierung nicht gefunden (ProblemDetail)")
    })
    public ResponseEntity<Void> deletePlacement(@PathVariable Long id) {
        placementService.deletePlacement(id);
        return ResponseEntity.noContent().build();
    }
}
