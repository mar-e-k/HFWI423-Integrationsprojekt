package de.fhdw.vendix.commons.core.printer.layout.components.table;

public final class TableLayout {

    public Table toTable() {
        return null;
    }

    public void fromTable(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("Parameter 'table' cannot be null");
        }
    }
}