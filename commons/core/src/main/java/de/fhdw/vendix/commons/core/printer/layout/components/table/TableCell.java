package de.fhdw.vendix.commons.core.printer.layout.components.table;

public record TableCell(
   float height,
   float width
) {
    public TableCell {
        if (height <= 0) {
            throw new IllegalArgumentException("Parameter 'height' cannot be below 0");
        }
        if (width <= 0) {
            throw new IllegalArgumentException("Parameter 'width' cannot be below 0");
        }
    }
}