package de.fhdw.vendix.store.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRole;
import de.fhdw.vendix.commons.ui.vaadin.view.AbstractRootView;
import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.api.envelope.EventEnvelopeBuilder;
import io.github.plaguv.amqp.api.event.logistic.NewDealEvent;
import io.github.plaguv.amqp.api.event.pos.ArticleOrderEvent;
import io.github.plaguv.amqp.api.event.pos.ArticleUrgentOrderEvent;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
import jakarta.annotation.security.RolesAllowed;

@Route("")
@RolesAllowed(AccountRole.ROLE_ADMIN)
public class RootView extends AbstractRootView {

    private final EventPublisher publisher;

    private BigDecimalField storeId;
    private BigDecimalField articleId;
    private BigDecimalField quantity;

    public RootView(EventPublisher publisher) {
        this.publisher = publisher;
        configureSettings();
        configureSubmitOrderButton();
        configureSubmitUrgentOrderButton();
        configureNewDealButton();
    }

    private void configureSettings() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        storeId = new BigDecimalField("Store ID");

        articleId = new BigDecimalField("Article ID");

        quantity = new BigDecimalField("Article Quantity");

        horizontalLayout.add(storeId, articleId, quantity);
        add(horizontalLayout);
    }

    private void configureSubmitOrderButton() {
        Button submitButton = new Button("Submit Order");
        submitButton.addClickListener(event -> {
                    ArticleOrderEvent articleOrderEvent = new ArticleOrderEvent(
                            storeId.getValue().longValue(),
                            articleId.getValue().longValue(),
                            quantity.getValue().longValue()
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
        Button submitButton = new Button("Submit Urgent Order");
        submitButton.addClickListener(event -> {
                    ArticleUrgentOrderEvent articleUrgentOrderEvent = new ArticleUrgentOrderEvent(
                            storeId.getValue().longValue(),
                            articleId.getValue().longValue(),
                            quantity.getValue().longValue()
                    );
                    EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                            .withContent(articleUrgentOrderEvent)
                            .build();
                    publisher.publishMessage(envelope);
                }
        );
        add(submitButton);
    }

    private void configureNewDealButton() {
        Button submitButton = new Button("Submit New Deal");
        submitButton.addClickListener(event -> {
                    NewDealEvent newDealEvent = new NewDealEvent(
                            5L
                    );
                    EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                            .withContent(newDealEvent)
                            .build();
                    publisher.publishMessage(envelope);
                }
        );
        add(submitButton);
    }
}