package de.fhdw.vendix.commons.core.printer.layout;

import de.fhdw.vendix.commons.core.printer.renderer.utility.TextStyle;
import de.fhdw.vendix.commons.core.printer.renderer.utility.TextWrapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public final class LayoutEngine {

    private final LayoutContext context;
    private final PDDocument document;

    private PDPage currentPage;
    private PDPageContentStream stream;

    private float cursorX;
    private float cursorY;

    private static final float GAP_SPACE = 4;
    private static final float MARGIN_LEFT = 40;
    private static final float MARGIN_RIGHT = 40;
    private static final float MARGIN_TOP = 750;
    private static final float MARGIN_BOTTOM = 50;

    public LayoutEngine(LayoutContext context, PDDocument document) throws IOException {
        this.context = context;
        this.document = document;
        pageBreak(); // initialize first page
    }

    public void text(String text, TextStyle style) throws IOException {
        float requiredSpace = style.getSize() + GAP_SPACE;
        ensureVerticalSpace(requiredSpace);
        context.drawText(stream, text, style, cursorX, cursorY);
        cursorY -= requiredSpace;
    }

    public void wrapText(String text, TextStyle style) throws IOException {
        float availableWidth = currentPage.getMediaBox().getWidth() - cursorX - MARGIN_RIGHT;
        List<String> lines = TextWrapper.splitTextInLines(text, style, availableWidth);
        for (String line : lines) {
            text(line, style);
        }
    }

    public void separator() throws IOException {
        float rightX = currentPage.getMediaBox().getWidth() - MARGIN_RIGHT;
        context.drawLine(stream, MARGIN_LEFT, cursorY, rightX, cursorY);
        cursorY -= GAP_SPACE * 2;
    }

    private void pageBreak() throws IOException {
        currentPage = new PDPage();
        document.addPage(currentPage);
        stream = new PDPageContentStream(document, currentPage);
        cursorY = MARGIN_TOP;
        cursorX = MARGIN_LEFT;
    }

    private boolean isVerticalSpaceEnough(float height) {
        return ((cursorY - height) >= MARGIN_BOTTOM);
    }

    private void ensureVerticalSpace(float height) throws IOException {
        if (!isVerticalSpaceEnough(height)) {
            pageBreak();
        }
    }

    public byte[] finish() throws IOException {
        stream.close();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        return out.toByteArray();
    }
}