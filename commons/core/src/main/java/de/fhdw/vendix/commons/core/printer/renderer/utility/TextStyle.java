package de.fhdw.vendix.commons.core.printer.renderer.utility;

import org.apache.pdfbox.pdmodel.font.*;

public record TextStyle(
        PDFont font,
        float size
) {
    public TextStyle {
        if (font == null) {
            throw new IllegalArgumentException("TextStyle parameter 'font' cannot be null");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("TextStyle parameter 'size' must be greater than 0");
        }
    }

    public TextStyle() {
        this(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
    }

    public TextStyle(PDFont font) {
        this(font, 12);
    }

    public TextStyle(float size) {
        this(new PDType1Font(Standard14Fonts.FontName.HELVETICA), size);
    }
}