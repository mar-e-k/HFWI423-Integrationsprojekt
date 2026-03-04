package de.fhdw.vendix.commons.core.printer.layout.components.table;

public record Table(
        String[] cellHeaders,
        float[] cellWidths,
        int rows,
        boolean drawBorders
) {
    public Table {
        if (cellHeaders == null) {
            throw new IllegalArgumentException("Table parameter 'cellHeaders' cannot be null");
        }
        if (cellWidths == null) {
            throw new IllegalArgumentException("Table parameter 'cellWidths' cannot be null");
        }
        if (rows <= 0) {
            throw new IllegalArgumentException("Table parameter 'rows' must be greater than 0");
        }
    }
}