package de.fhdw.vendix.pos.ui.register.action_pad;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;

import java.util.function.Supplier;

public final class ColorButtonGrid extends FormLayout {

    public ColorButtonGrid(Supplier<Button> red,
                           Supplier<Button> green,
                           Supplier<Button> blue,
                           Supplier<Button> purple,
                           Supplier<Button> yellow,
                           Supplier<Button> brown) {

        addFormRow(red.get(), green.get(), blue.get());
        addFormRow(purple.get(), yellow.get(), brown.get());
        setAutoResponsive(true);
    }
}