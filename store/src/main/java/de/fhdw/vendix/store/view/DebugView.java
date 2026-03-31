package de.fhdw.vendix.store.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.aura.Aura;
import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.api.envelope.EventEnvelopeBuilder;
import io.github.plaguv.amqp.api.event.pos.ArticleOrderEvent;
import io.github.plaguv.amqp.api.event.pos.ArticleUrgentOrderEvent;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
import jakarta.annotation.security.RolesAllowed;

@Route("debug")
@StyleSheet(Aura.STYLESHEET)
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class DebugView extends VerticalLayout {

    private final EventPublisher eventPublisher;

    private final BigDecimalField storeIdField = new BigDecimalField("Store ID");
    private final BigDecimalField articleIdField = new BigDecimalField("Article ID");
    private final BigDecimalField articleQuantityField = new BigDecimalField("Article Quantity");

    public DebugView(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        configureDebugButton();
        add(new HorizontalLayout(storeIdField, articleIdField, articleQuantityField));
        configureArticleOrderButton();
        configureUrgentArticleOrderButton();
    }

    private void configureDebugButton() {
        Button button = new Button("Debug Exception");
        button.addClickListener(event -> {
            throw new IllegalArgumentException("Debug Exception");
        });
        super.add(button);
    }

    private void configureArticleOrderButton() {
        Button button = new Button("Article Order");
        button.addClickListener(event -> {
            ArticleOrderEvent articleOrderEvent = new ArticleOrderEvent(
                    storeIdField.getValue().longValue(),
                    articleIdField.getValue().longValue(),
                    articleQuantityField.getValue().longValue()
            );
            EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                    .withContent(articleOrderEvent)
                    .build();
            eventPublisher.publishMessage(envelope);
        });
        add(button);
    }

    private void configureUrgentArticleOrderButton() {
        Button button = new Button("Urgent Article Order");
        button.addClickListener(event -> {
            ArticleUrgentOrderEvent articleUrgentOrderEvent = new ArticleUrgentOrderEvent(
                    storeIdField.getValue().longValue(),
                    articleIdField.getValue().longValue(),
                    articleQuantityField.getValue().longValue()
            );
            EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                    .withContent(articleUrgentOrderEvent)
                    .build();
            eventPublisher.publishMessage(envelope);
        });
    }
}