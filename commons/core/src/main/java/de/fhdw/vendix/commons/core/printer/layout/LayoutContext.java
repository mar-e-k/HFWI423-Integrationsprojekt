package de.fhdw.vendix.commons.core.printer.layout;

import de.fhdw.vendix.commons.core.printer.renderer.utility.TextStyle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

public final class LayoutContext {

    public void drawText(PDPageContentStream stream, String text, TextStyle style, float x, float y) throws IOException {
        stream.beginText();
        stream.setFont(style.getFont(), style.getSize());
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }

    public void drawLine(PDPageContentStream stream, float x_from, float y_from, float x_to, float y_to) throws IOException {
        stream.moveTo(x_from, y_from);
        stream.lineTo(x_to, y_to);
        stream.stroke();
    }
}