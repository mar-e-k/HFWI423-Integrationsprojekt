package de.fhdw.vendix.store.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.ui.vaadin.view.AbstractRootView;
import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.api.envelope.EventEnvelopeBuilder;
import io.github.plaguv.amqp.api.event.logistic.ArticleSentEvent;
import io.github.plaguv.amqp.api.event.pos.ArticleOrderEvent;
import io.github.plaguv.amqp.api.event.pos.ArticleUrgentOrderEvent;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
import jakarta.annotation.security.RolesAllowed;

@Route("")
@RolesAllowed("ADMIN")
public class RootView extends AbstractRootView {

    private final EventPublisher publisher;

    public RootView(EventPublisher publisher) {
        this.publisher = publisher;
        configureSubmitOrderButton();
        configureSubmitUrgentOrderButton();
    }

    private void configureSubmitOrderButton() {
        Button submitButton = new Button("Submit");
        submitButton.addClickListener(event -> {
                    ArticleOrderEvent articleOrderEvent = new ArticleOrderEvent(
                            1L,
                            1L,
                            5
                    );
                    EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                            .withContent(articleOrderEvent)
                            .build();
                    publisher.publishMessage(envelope);
                }
        );
        add(submitButton);
    }

    private void configureSubmitUrgentOrderButton() {
        Button submitButton = new Button("Submit");
        submitButton.addClickListener(event -> {
                    ArticleUrgentOrderEvent articleUrgentOrderEvent = new ArticleUrgentOrderEvent(
                            1L,
                            1L,
                            5
                    );
                    EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                            .withContent(articleUrgentOrderEvent)
                            .build();
                    publisher.publishMessage(envelope);
                }
        );
        add(submitButton);
    }
}