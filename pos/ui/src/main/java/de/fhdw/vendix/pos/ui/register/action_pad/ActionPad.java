package de.fhdw.vendix.pos.ui.register.action_pad;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhdw.vendix.pos.ui.register.RegisterState;

public final class ActionPad extends VerticalLayout {

    private final RegisterState state;
    private final ArticleAmountDisplay display = new ArticleAmountDisplay();

    public ActionPad(RegisterState state, Runnable onAdd) {
        this.state = state;

        setWidthFull();
        setSpacing(true);

        ColorButtonGrid colorGrid = new ColorButtonGrid(
                () -> createColorButton("Red"),
                () -> createColorButton("Green"),
                () -> createColorButton("Blue"),
                () -> createColorButton("Purple"),
                () -> createColorButton("Yellow"),
                () -> createColorButton("Brown")
        );

        NumberPad numpad = new NumberPad(
                this::appendDigit,
                this::clear,
                this::backspace
        );

        Button addButton = new Button("Add", e -> onAdd.run());
        addButton.setWidthFull();

        HorizontalLayout buttonsLayout = new HorizontalLayout(colorGrid, numpad);

        add(display, buttonsLayout, addButton);

        state.addListener(this::refresh);

        refresh();
    }

    private void appendDigit(int digit) {
        int current = state.getAmount();

        int next = (current == 0)
                ? digit
                : Integer.parseInt(current + "" + digit);

        state.setAmount(next);
    }

    private void clear() {
        state.setAmount(1);
    }

    private void backspace() {
        String s = String.valueOf(state.getAmount());

        int next = (s.length() > 1)
                ? Integer.parseInt(s.substring(0, s.length() - 1))
                : 1;

        state.setAmount(next);
    }

    private void refresh() {
        display.setValue(state.getAmount());
    }

    private Button createColorButton(String label) {
        Button btn = new Button(label);
        btn.setWidthFull();

        btn.addClickListener(e -> {
        });

        return btn;
    }
}