package de.fhdw.vendix.commons.core.printer.layout.components.table;

import java.util.ArrayList;
import java.util.List;

public final class Table {

    private final List<TableRow> rows;

    public Table() {
        rows = new ArrayList<>();
    }

    public Table(int row_count, int column_count) {
        if (row_count < 0) {
            throw new IllegalArgumentException("Parameter 'row_count' cannot be negative");
        }
        if (column_count < 0) {
            throw new IllegalArgumentException("Parameter 'column_count' cannot be negative");
        }

        rows = new ArrayList<>();

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
    }

    public void appendRow() {
        if (rows.isEmpty()) {
            rows.add(TableRow.returnEmptyCellsRow(1));
            return;
        }
        rows.add(TableRow.returnEmptyCellsRow(rows.getFirst().size()));
    }

    public void appendColumn() {
        if (rows.isEmpty()) {
            rows.add(TableRow.returnEmptyCellsRow(1));
            return;
        }
        for (TableRow row : rows) {
            row.add(new TableCell());
        }
    }
}