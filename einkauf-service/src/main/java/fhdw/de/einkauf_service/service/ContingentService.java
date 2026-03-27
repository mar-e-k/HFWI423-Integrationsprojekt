package fhdw.de.einkauf_service.service;

import fhdw.de.einkauf_service.dto.ContingentResponseDTO;

import java.util.List;

public interface ContingentService {

    List<ContingentResponseDTO> getAllAvailableContingents();

    void deleteContingent(Long contingentId);
}
