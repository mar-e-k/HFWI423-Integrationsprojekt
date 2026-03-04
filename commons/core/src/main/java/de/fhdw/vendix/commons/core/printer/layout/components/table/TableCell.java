package de.fhdw.vendix.commons.core.printer.layout.components.table;

import de.fhdw.vendix.commons.core.printer.renderer.utility.TextBlock;

public final class TableCell {

    private TextBlock content;

    public TableCell() {
        content = TextBlock.empty();
    }

    public TableCell(TextBlock content) {
        if (content == null) {
            throw new IllegalArgumentException("TableCell parameter 'content' cannot be null");
        }
        this.content = content;
    }

    public TextBlock getContent() {
        return content;
    }
}