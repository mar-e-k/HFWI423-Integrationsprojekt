package de.fhdw.vendix.commons.core.printer.layout.components.table;

import de.fhdw.vendix.commons.core.printer.layout.LayoutEngine;

import java.io.IOException;

public final class TableLayout {

    private final LayoutEngine engine;
//    private Table table;

    public TableLayout(LayoutEngine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("Parameter 'engine' cannot be null.");
        }
        this.engine = engine;
    }

    public void render() throws IOException {

    }
}