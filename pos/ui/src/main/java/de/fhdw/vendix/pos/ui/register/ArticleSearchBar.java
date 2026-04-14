package de.fhdw.vendix.pos.ui.register;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;

public final class ArticleSearchBar extends HorizontalLayout {

    private final TextField input = new TextField();
    private final Button search = new Button(VaadinIcon.SEARCH.create());

    public ArticleSearchBar(Consumer<String> onSearch) {
        setWidthFull();

        input.setPlaceholder("Scan or enter GTIN");
        input.setPattern("\\d*");
        input.setWidthFull();

        input.addKeyPressListener(Key.ENTER, e ->
                onSearch.accept(input.getValue())
        );

        search.addClickListener(e ->
                onSearch.accept(input.getValue())
        );

        add(input, search);
        expand(input);
    }
}