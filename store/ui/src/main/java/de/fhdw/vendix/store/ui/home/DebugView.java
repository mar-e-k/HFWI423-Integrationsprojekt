package de.fhdw.vendix.store.ui.home;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakRole;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.api.envelope.EventEnvelopeBuilder;
import io.github.plaguv.amqp.api.event.pos.ArticleOrderEvent;
import io.github.plaguv.amqp.api.event.pos.ArticleUrgentOrderEvent;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "debug", layout = StoreAppLayout.class)
@RolesAllowed(KeycloakRole.Constants.ADMIN)
public class DebugView extends VerticalLayout {

    private final EventPublisher publisher;

    public DebugView(EventPublisher publisher) {
        this.publisher = publisher;
        TabSheet tabs = new TabSheet();
        createStoreDebugTab(tabs);
        createMessagingDebugTab(tabs);
        createGenerationDebugTab(tabs);
        add(tabs);
    }

    private void createStoreDebugTab(TabSheet tabSheet) {
        tabSheet.add("Store", new VerticalLayout());
    }

    private void createMessagingDebugTab(TabSheet tabSheet) {
        VerticalLayout articleOrderLayout = new VerticalLayout();
        HorizontalLayout articleOrderConfigurationLayout = new HorizontalLayout();

        IntegerField storeIdField = new IntegerField("Store id");
        storeIdField.setMin(1);
        storeIdField.setPlaceholder("1");
        IntegerField articleIdField = new IntegerField("Article id");
        articleIdField.setMin(1);
        articleIdField.setPlaceholder("1");
        IntegerField articleQuantityField = new IntegerField("Article quantity");
        articleQuantityField.setMin(1);
        articleQuantityField.setPlaceholder("1");

        HorizontalLayout articleOrderSubmitLayout = new HorizontalLayout();
        articleOrderSubmitLayout.add(createArticleOrderButton(
                storeIdField, articleIdField, articleQuantityField
        ));
        articleOrderSubmitLayout.add(createArticleUrgentOrderButton(
                storeIdField, articleIdField, articleQuantityField
        ));
        
        articleOrderConfigurationLayout.add(storeIdField, articleIdField, articleQuantityField);
        articleOrderLayout.add(articleOrderConfigurationLayout);
        articleOrderLayout.add(articleOrderSubmitLayout);
        tabSheet.add("Messaging", articleOrderLayout);
    }

    private Button createArticleOrderButton(
            IntegerField storeField,
            IntegerField articleField,
            IntegerField quantityField
    ) {
        Button orderButton = new Button("Submit Order");
        orderButton.addClickListener(event -> {
                    ArticleOrderEvent articleOrderEvent = new ArticleOrderEvent(
                            storeField.getValue().longValue(),
                            articleField.getValue().longValue(),
                            quantityField.getValue().longValue()
                    );
                    EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                            .withContent(articleOrderEvent)
                            .build();
                    publisher.publishMessage(envelope);
                }
        );
        return orderButton;
    }

    private Button createArticleUrgentOrderButton(
            IntegerField storeField,
            IntegerField articleField,
            IntegerField quantityField
    ) {
        Button urgentOrderButton = new Button("Submit Urgent Order");
        urgentOrderButton.addClickListener(event -> {
                    ArticleUrgentOrderEvent articleUrgentOrderEvent = new ArticleUrgentOrderEvent(
                            storeField.getValue().longValue(),
                            articleField.getValue().longValue(),
                            quantityField.getValue().longValue()
                    );
                    EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                            .withContent(articleUrgentOrderEvent)
                            .build();
                    publisher.publishMessage(envelope);
                }
        );
        return urgentOrderButton;
    }

    private void createGenerationDebugTab(TabSheet tabSheet) {
        tabSheet.add("Generation", new VerticalLayout());
    }
}