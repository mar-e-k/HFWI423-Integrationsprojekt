package de.fhdw.vendix.pos.ui.register.article_search;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.Set;

public final class ArticleSearchBar extends HorizontalLayout {

    private static final Set<Integer> VALID_LENGTHS = Set.of(8, 12, 13, 14);
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 14;

    private final TextField inputField = new TextField();
    private final Button searchButton = new Button(VaadinIcon.SEARCH.create());

    private final ArticleSearchController controller;

    public ArticleSearchBar(ArticleSearchController controller) {
        this.controller = controller;

        setWidthFull();
        configureInput();
        configureSearch();

        add(inputField, searchButton);
        expand(inputField);
    }

    private void configureInput() {
        inputField.setPlaceholder("Scan or enter GTIN");
        inputField.setWidthFull();
        inputField.setValueChangeMode(ValueChangeMode.EAGER);
        inputField.addValueChangeListener(event -> {
            validateAndUpdateState(event.getValue());
        });
        inputField.addKeyPressListener(Key.ENTER, e -> {
            search();
        });
    }

    private void validateAndUpdateState(String value) {
        if (value == null || value.isEmpty()) {
            inputField.setInvalid(false);
            inputField.setErrorMessage(null);
            searchButton.setEnabled(false);
            return;
        }

        if (!value.matches("\\d+")) {
            setInvalidState("Only digits are allowed");
            return;
        }

        int length = value.length();
        if (VALID_LENGTHS.contains(length)) {
            inputField.setInvalid(false);
            inputField.setErrorMessage(null);
            searchButton.setEnabled(true);
        } else {
            String message = calculateLengthErrorMessage(length);
            setInvalidState(message);
        }
    }

    private String calculateLengthErrorMessage(int length) {
        if (length < MIN_LENGTH) {
            return String.format("Too short (missing %d digits)", MIN_LENGTH - length);
        } else if (length > MAX_LENGTH) {
            return String.format("Too long (remove %d digits)", length - MAX_LENGTH);
        } else {
            return "Invalid length (must be 8, 12, 13, or 14 digits)";
        }
    }

    private void setInvalidState(String message) {
        inputField.setInvalid(true);
        inputField.setErrorMessage(message);
        searchButton.setEnabled(false);
    }

    private void configureSearch() {
        searchButton.addClickListener(e -> search());
    }

    private void search() {
        String value = inputField.getValue();
        if (value == null || !VALID_LENGTHS.contains(value.length()) || !value.matches("\\d+")) {
            setInvalidState("Invalid GTIN");
            return;
        }

        inputField.setInvalid(false);
        controller.search(value.trim());
    }
}