package de.fhdw.vendix.commons.core.printer.layout.components.table;

import java.util.ArrayList;
import java.util.List;

public final class TableRow {

    private final List<TableCell> cells;

    public TableRow() {
        cells = new ArrayList<>();
    }

    public TableCell get(int row) throws IndexOutOfBoundsException {
        return cells.get(row);
    }

    public void add(TableCell cell) {
        cells.add(cell);
    }

    public int size() {
        return cells.size();
    }

    public static TableRow returnEmptyCellsRow(int amount) {
        TableRow tableRow = new TableRow();
        for (int i = 0; i < amount; i++) {
            tableRow.add(new TableCell());
        }
        return tableRow;
    }
}