package fhdw.de.einkauf_service.service;

import fhdw.de.einkauf_service.dto.SupplierRequestDTO;
import fhdw.de.einkauf_service.dto.SupplierResponseDTO;

import java.util.List;

public interface SupplierService {

    /**
     * Legt einen neuen Lieferanten an und prüft dabei auf Namenseindeutigkeit.
     */
    SupplierResponseDTO createNewSupplier(SupplierRequestDTO newSupplierRequestDTO);

    /**
     * Liefert eine Liste aller Lieferanten.
     */
    List<SupplierResponseDTO> findAllSuppliers();

    /**
     * Liefert einen spezifischen Lieferanten anhand der ID.
     */
    SupplierResponseDTO findSupplierById(Long id);

    /**
     * Aktualisiert die Stammdaten eines bestehenden Lieferanten.
     */
    SupplierResponseDTO updateSupplier(Long id, SupplierRequestDTO updatedSupplierRequestDTO);

    /**
     * Löscht einen Lieferanten anhand der ID.
     */
    void deleteSupplier(Long id);
}