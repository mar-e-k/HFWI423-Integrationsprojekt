package de.fhdw.vendix.pos.ui.register;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.function.Consumer;


public class ActionPad extends VerticalLayout {

    private int currentAmount = 1;
    private final Span display = new Span("1");

    public ActionPad(Consumer<Integer> onAdd) {
        setWidthFull();
        setSpacing(true);

        display.getStyle()
                .set("font-size", "2em")
                .set("font-weight", "bold");

        FormLayout buttonLayout = new FormLayout();
        buttonLayout.addFormRow(
                createColorButton("Red"),
                createColorButton("Green"),
                createColorButton("Blue")
        );
        buttonLayout.addFormRow(
                createColorButton("Purple"),
                createColorButton("Yellow"),
                createColorButton("Brown")
        );
        buttonLayout.setAutoResponsive(true);

        Div numpad = new Div();
        numpad.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(3, 1fr)")
                .set("gap", "8px")
                .set("width", "200px");

        for (int i = 1; i <= 9; i++) {
            int num = i;
            numpad.add(createNumberButton(String.valueOf(i), () -> appendNumber(num)));
        }

        numpad.add(createNumberButton("0", () -> appendNumber(0)));
        numpad.add(new Button("C", e -> clear()));
        numpad.add(new Button("←", e -> backspace()));

        Button addButton = new Button("Add", e -> onAdd.accept(currentAmount));
        addButton.setWidthFull();

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.add(buttonLayout, numpad);

        add(display, buttonsLayout, addButton);
    }

    private Button createColorButton(String label) {
        Button btn = new Button(label);
        btn.setWidthFull();
        return btn;
    }

    private Button createNumberButton(String text, Runnable action) {
        Button btn = new Button(text, e -> action.run());
        btn.setWidthFull();
        return btn;
    }

    private void appendNumber(int num) {
        if (currentAmount == 0) {
            currentAmount = num;
        } else {
            currentAmount = Integer.parseInt(String.valueOf(currentAmount) + num);
        }
        updateDisplay();
    }

    private void clear() {
        currentAmount = 1;
        updateDisplay();
    }

    private void backspace() {
        String s = String.valueOf(currentAmount);
        if (s.length() > 1) {
            currentAmount = Integer.parseInt(s.substring(0, s.length() - 1));
        } else {
            currentAmount = 1;
        }
        updateDisplay();
    }

    private void updateDisplay() {
        display.setText(String.valueOf(currentAmount));
    }
}