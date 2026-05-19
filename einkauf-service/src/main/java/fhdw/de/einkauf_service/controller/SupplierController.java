package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.SupplierRequestDTO;
import fhdw.de.einkauf_service.dto.SupplierResponseDTO;
import fhdw.de.einkauf_service.service.SupplierService;
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
@RequestMapping("/api/v1/suppliers")
@Tag(name = "Suppliers", description = "Verwaltung der Lieferanten")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Neuen Lieferanten anlegen")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lieferant angelegt"),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabe (ProblemDetail)"),
            @ApiResponse(responseCode = "409", description = "Lieferant mit gleichem Namen existiert bereits (ProblemDetail)")
    })
    public SupplierResponseDTO createSupplier(@Valid @RequestBody SupplierRequestDTO supplierRequestDTO) {
        return supplierService.createNewSupplier(supplierRequestDTO);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Alle Lieferanten auflisten")
    public List<SupplierResponseDTO> getAllSuppliers() {
        return supplierService.findAllSuppliers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lieferant per ID abrufen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lieferant gefunden"),
            @ApiResponse(responseCode = "404", description = "Lieferant nicht gefunden (ProblemDetail)")
    })
    public ResponseEntity<SupplierResponseDTO> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.findSupplierById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Lieferant aktualisieren")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lieferant aktualisiert"),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabe (ProblemDetail)"),
            @ApiResponse(responseCode = "404", description = "Lieferant nicht gefunden (ProblemDetail)"),
            @ApiResponse(responseCode = "409", description = "Lieferantenname kollidiert (ProblemDetail)")
    })
    public ResponseEntity<SupplierResponseDTO> updateSupplier(@PathVariable Long id,
                                                              @Valid @RequestBody SupplierRequestDTO supplierRequestDTO) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, supplierRequestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Lieferant löschen")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Lieferant gelöscht"),
            @ApiResponse(responseCode = "404", description = "Lieferant nicht gefunden (ProblemDetail)")
    })
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
