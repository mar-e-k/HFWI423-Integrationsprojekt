package de.fhdw.vendix.store.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.ui.vaadin.view.AbstractRootView;
import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.api.envelope.EventEnvelopeBuilder;
import io.github.plaguv.amqp.api.event.pos.LogisticArticleOrderEvent;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
import jakarta.annotation.security.RolesAllowed;

@Route("")
@RolesAllowed("ADMIN")
public class RootView extends AbstractRootView {

    private final EventPublisher publisher;

    public RootView(EventPublisher publisher) {
        this.publisher = publisher;
        configureSubmitButton();
    }

    private void configureSubmitButton() {
        Button submitButton = new Button("Submit");
        submitButton.addClickListener(event -> {
                    EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                            .withContentType(LogisticArticleOrderEvent.class)
                            .build();
                    publisher.publishMessage(envelope);
                }
        );
        add(submitButton);
    }
}