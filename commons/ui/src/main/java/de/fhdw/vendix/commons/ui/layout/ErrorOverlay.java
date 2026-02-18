package de.fhdw.vendix.commons.ui.layout;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import de.fhdw.vendix.commons.ui.utlity.ErrorPayload;

import java.time.Duration;

public final class ErrorOverlay {

    private ErrorOverlay() {}

    private static Dialog createErrorDialog(ErrorPayload payload) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(true);
        dialog.getElement().getThemeList().add("error-overlay");
        dialog.setSizeFull();
        dialog.setWidthFull();

        TextArea stackTrace = new TextArea("Details");
        stackTrace.setValue(payload.stackTrace());
        stackTrace.setReadOnly(true);
        stackTrace.setWidthFull();

        Button copy = new Button("Copy", e ->
                UI.getCurrent().getPage().executeJs(
                        """
                                import('/frontend/components/error-overlay/clipboard.js')
                                  .then(m => m.copyToClipboard($0))
                                  .catch(() => alert('Clipboard not supported'));
                                """,
                        payload.stackTrace()
                )
        );

        Button close = new Button("Close", e -> dialog.close());

        HorizontalLayout actions = new HorizontalLayout(copy, close);

        dialog.add(
                new H3(payload.title()),
                new Span(payload.message()),
                stackTrace,
                actions
        );

        return dialog;
    }

    public static void show(ErrorPayload payload) {
        show(payload, Duration.ofSeconds(10));
    }

    public static void show(ErrorPayload payload, Duration openForDuration) {
        Dialog dialog = createErrorDialog(payload);
        dialog.open();
        UI.getCurrent().getPage().executeJs(
                "import('/frontend/components/error-overlay/error-overlay.js').then(m => m.autoCloseDialog($0, $1));",
                dialog.getElement(),
                (int) openForDuration.toMillis() // primitive long not supported
        );
    }
}