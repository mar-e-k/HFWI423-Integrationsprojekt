package de.fhdw.vendix.commons.core.printer.renderer;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;

import java.util.List;

public interface ReceiptRenderer {
    byte[] render(ReceiptDTO receipt);
    List<SectionRenderer> sections();
}