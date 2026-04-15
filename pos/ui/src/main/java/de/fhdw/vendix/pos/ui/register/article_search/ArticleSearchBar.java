package de.fhdw.vendix.pos.ui.register.article_search;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class ArticleSearchBar extends HorizontalLayout {

    private static final Pattern GTIN_PATTERN = Pattern.compile("^\\d{8}(\\d{4}|\\d{5}|\\d{6})?$");

    private final TextField input = new TextField();
    private final Button search = new Button(VaadinIcon.SEARCH.create());

    public ArticleSearchBar(Consumer<String> onSearch) {
        setWidthFull();

        input.setPlaceholder("Scan or enter GTIN");
        input.setWidthFull();

        input.setPattern("^\\d{8}(\\d{4}|\\d{5}|\\d{6})?$");
        input.setErrorMessage("Invalid GTIN format");

        search.addClickListener(e -> trySearch(onSearch));
        input.addKeyPressListener(Key.ENTER, e -> trySearch(onSearch));

        add(input, search);
        expand(input);
    }

    private void trySearch(Consumer<String> onSearch) {
        String value = input.getValue() == null ? "" : input.getValue().trim();

        if (!GTIN_PATTERN.matcher(value).matches()) {
            input.setInvalid(true);
            return;
        }

        input.setInvalid(false);
        onSearch.accept(value);
    }
}