package de.fhdw.vendix.commons.api.domain.receipt;

import de.fhdw.vendix.commons.api.structure.dto.ResponseDTO;

public record ReceiptResponseDTO(

) implements ResponseDTO {
    @Deprecated
    public ReceiptResponseDTO {
        throw new UnsupportedOperationException("ReceiptResponseDTO has not yet been implemented");
    }
}