package de.fhdw.vendix.commons.core.printer.renderer.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.core.printer.renderer.ReceiptRenderer;
import de.fhdw.vendix.commons.core.printer.renderer.SectionRenderer;

import java.util.List;

public final class VoucherReceiptRenderer implements ReceiptRenderer {

    @Override
    public byte[] render(ReceiptDTO receipt) {
        return new byte[0];
    }

    @Override
    public List<SectionRenderer> sections() {
        return List.of();
    }
}
