package de.fhdw.vendix.commons.core.printer.renderer.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.core.printer.layout.LayoutContext;
import de.fhdw.vendix.commons.core.printer.layout.LayoutEngine;
import de.fhdw.vendix.commons.core.printer.renderer.ReceiptRenderer;
import de.fhdw.vendix.commons.core.printer.renderer.SectionRenderer;
import de.fhdw.vendix.commons.core.printer.renderer.section.HeaderSection;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.List;

public final class StandardReceiptRenderer implements ReceiptRenderer {

    @Override
    public byte[] render(ReceiptDTO receipt) {
        try {
            LayoutContext context = new LayoutContext();
            PDDocument document = new PDDocument();
            LayoutEngine engine = new LayoutEngine(context, document);

            for (SectionRenderer section : sections()) {
                section.render(receipt, engine);
            }

            return engine.finish();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SectionRenderer> sections() {
        return List.of(
                new HeaderSection()
        );
    }
}