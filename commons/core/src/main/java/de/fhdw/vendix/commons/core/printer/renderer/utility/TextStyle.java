package de.fhdw.vendix.commons.core.printer.renderer.utility;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public final class TextStyle {

    private PDFont font;
    private float size;

    public TextStyle(PDFont font, float size) {
        if (font == null) {
            throw new IllegalArgumentException("Parameter 'font' cannot be null");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Parameter 'size' must be greater than zero");
        }
        this.font = font;
        this.size = size;
    }

    public static TextStyle defaultStyle() {
        return new TextStyle(
                new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                12
        );
    }

    public PDFont getFont() {
        return font;
    }

    public void setFont(PDFont font) {
        if (font == null) {
            throw new IllegalArgumentException("Parameter 'font' cannot be null");
        }
        this.font = font;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Parameter 'size' must be greater than zero");
        }
        this.size = size;
    }
}