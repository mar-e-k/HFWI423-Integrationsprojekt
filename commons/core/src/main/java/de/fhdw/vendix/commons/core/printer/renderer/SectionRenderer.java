package de.fhdw.vendix.commons.core.printer.renderer;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.core.printer.layout.LayoutEngine;

public interface SectionRenderer {
    void render(ReceiptDTO receipt, LayoutEngine engine);
}