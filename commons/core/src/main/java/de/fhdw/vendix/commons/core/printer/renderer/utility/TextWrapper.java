package de.fhdw.vendix.commons.core.printer.renderer.utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class TextWrapper {

    private TextWrapper() {}

    /**
     * Confines the text in the given width and splits it up into lines, if required
     * @param text that should be split up, if required
     * @param style of the text
     * @param availableWidth in which the text can be extended
     * @return a {@code list} containing all the required lines of the original {@code text} for it to be confined in the given {@code width}
     * @throws IOException if the given font in {@code style} cannot be read from
     */
    public static List<String> splitTextInLines(String text, TextStyle style, float availableWidth) throws IOException {
        List<String> lines = new ArrayList<String>();
        StringBuilder currentLine = new StringBuilder();

        for (String word : text.split(" ")) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            float width = style.getFont().getStringWidth(testLine) / 1000 * style.getSize(); // convert to pixel
            if (width > availableWidth) {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }
        return lines;
    }
}