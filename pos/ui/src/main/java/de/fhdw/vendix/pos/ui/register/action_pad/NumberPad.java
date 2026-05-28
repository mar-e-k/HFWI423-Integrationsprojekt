package de.fhdw.vendix.pos.ui.register.action_pad;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;

import java.util.function.Consumer;

public final class NumberPad extends Div {

    public NumberPad(Consumer<Integer> onDigit,
                     Runnable onClear,
                     Runnable onBackspace) {

        getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(3, 1fr)")
                .set("gap", "8px")
                .set("width", "200px");

        for (int i = 1; i <= 9; i++) {
            int num = i;
            add(createButton(String.valueOf(i), () -> onDigit.accept(num)));
        }

        add(createButton("0", () -> onDigit.accept(0)));
        add(createButton("C", onClear));
        add(createButton("←", onBackspace));
    }

    private Button createButton(String text, Runnable action) {
        return new Button(text, e -> action.run());
    }
}