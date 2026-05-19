package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.ShelfRequestDTO;
import fhdw.de.einkauf_service.dto.ShelfResponseDTO;
import fhdw.de.einkauf_service.dto.ShelfLevelResponseDTO;
import fhdw.de.einkauf_service.service.ShelfService;
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
@RequestMapping("/api/v1/shelves")
@Tag(name = "Shelves", description = "Verwaltung der Regale und Regalebenen im Lager")
public class ShelfController {

    private final ShelfService shelfService;

    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Neues Regal anlegen")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Regal angelegt"),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabe (ProblemDetail)"),
            @ApiResponse(responseCode = "404", description = "Referenzierte Kategorie nicht gefunden (ProblemDetail)")
    })
    public ShelfResponseDTO createShelf(@Valid @RequestBody ShelfRequestDTO shelfRequestDTO) {
        return shelfService.createShelf(shelfRequestDTO);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Alle Regale auflisten")
    @ApiResponse(responseCode = "200", description = "Liste aller Regale")
    public List<ShelfResponseDTO> getAllShelves() {
        return shelfService.getAllShelves();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Regal per ID abrufen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regal gefunden"),
            @ApiResponse(responseCode = "404", description = "Regal nicht gefunden (ProblemDetail)")
    })
    public ResponseEntity<ShelfResponseDTO> getShelfById(@PathVariable Long id) {
        ShelfResponseDTO shelf = shelfService.getShelf(id);
        return ResponseEntity.ok(shelf);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Regale einer Kategorie abrufen")
    @ApiResponse(responseCode = "200", description = "Trefferliste")
    public ResponseEntity<List<ShelfResponseDTO>> getShelfsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(shelfService.getShelfsByCategory(categoryId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Regal aktualisieren (Name/Beschreibung)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regal aktualisiert"),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabe (ProblemDetail)"),
            @ApiResponse(responseCode = "404", description = "Regal nicht gefunden (ProblemDetail)")
    })
    public ResponseEntity<ShelfResponseDTO> updateShelf(@PathVariable Long id,
                                                        @Valid @RequestBody ShelfRequestDTO shelfRequestDTO) {
        return ResponseEntity.ok(shelfService.updateShelf(id, shelfRequestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Regal löschen")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Regal gelöscht"),
            @ApiResponse(responseCode = "404", description = "Regal nicht gefunden (ProblemDetail)")
    })
    public ResponseEntity<Void> deleteShelf(@PathVariable Long id) {
        shelfService.deleteShelf(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{shelfId}/levels")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ebene zu Regal hinzufügen")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ebene angelegt"),
            @ApiResponse(responseCode = "400", description = "Ungültige Position oder bereits 5 Ebenen vorhanden (ProblemDetail)"),
            @ApiResponse(responseCode = "404", description = "Regal nicht gefunden (ProblemDetail)")
    })
    public ShelfLevelResponseDTO addLevel(@PathVariable Long shelfId,
                                          @RequestParam Integer levelPosition) {
        return shelfService.addLevel(shelfId, levelPosition);
    }

    @DeleteMapping("/{shelfId}/levels/{levelPosition}")
    @Operation(summary = "Ebene aus Regal entfernen")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ebene entfernt"),
            @ApiResponse(responseCode = "404", description = "Regal oder Ebene nicht gefunden (ProblemDetail)")
    })
    public ResponseEntity<Void> removeLevel(@PathVariable Long shelfId,
                                            @PathVariable Integer levelPosition) {
        shelfService.removeLevel(shelfId, levelPosition);
        return ResponseEntity.noContent().build();
    }
}
