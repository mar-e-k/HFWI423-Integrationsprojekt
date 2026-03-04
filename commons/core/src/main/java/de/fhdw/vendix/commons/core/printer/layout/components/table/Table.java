package de.fhdw.vendix.commons.core.printer.layout.components.table;

import java.util.ArrayList;
import java.util.List;

public final class Table {

    private final List<TableRow> rows = new ArrayList<>();

    private int width;
    private int height;

    public Table() {}

    public Table(int row_count, int column_count) {
        if (row_count < 0) {
            throw new IllegalArgumentException("Parameter 'row_count' cannot be negative");
        }
        if (column_count < 0) {
            throw new IllegalArgumentException("Parameter 'column_count' cannot be negative");
        }

        for (int i = 0; i < row_count; i++) {
            appendRow();
        }
        for (int i = 0; i < column_count; i++) {
            appendColumn();
        }
    }

    public TableCell getCell(int row, int column) throws IndexOutOfBoundsException {
        if (row < 0) {
            throw new IllegalArgumentException("Parameter 'row' cannot be negative");
        }
        if (column < 0) {
            throw new IllegalArgumentException("Parameter 'column' cannot be negative");
        }

        return rows.get(row).get(column);
    }

    public void setCell(TableCell cell, int row, int column) throws IndexOutOfBoundsException {
        if (cell == null) {
            throw new IllegalArgumentException("Parameter 'cell' cannot be null");
        }
        if (row < 0) {
            throw new IllegalArgumentException("Parameter 'row' cannot be negative");
        }
        if (column < 0) {
            throw new IllegalArgumentException("Parameter 'column' cannot be negative");
        }
        // TODO
    }

    public void appendRow() {
        if (rows.isEmpty()) {
            rows.add(TableRow.returnEmptyCellsRow(1));
        } else {
            rows.add(TableRow.returnEmptyCellsRow(rows.getFirst().size()));
        }
        height++;
    }

    public void appendColumn() {
        if (rows.isEmpty()) {
            rows.add(TableRow.returnEmptyCellsRow(1));
        } else {
            for (TableRow row : rows) {
                row.add(new TableCell());
            }
        }
        width++;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}