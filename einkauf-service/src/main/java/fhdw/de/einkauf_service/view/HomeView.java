package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route(value = "", layout = MainLayout.class)
public class HomeView extends VerticalLayout {

    public HomeView() {
        addClassName("home-view");

        H1 heroTitle = new H1("Willkommen im Einkaufssystem");
        Paragraph heroDesc = new Paragraph(
                "Verwalten Sie Artikel, Lieferanten, Bestellungen und Kontingente zentral an einem Ort.");

        Div hero = new Div(heroTitle, heroDesc);
        hero.addClassName("home-hero");
        add(hero);

        Div cards = new Div();
        cards.addClassName("home-cards");

        cards.add(createCard("📦", "Artikel",        "Artikelstammdaten, Kategorien und Preise verwalten.", ArticleView.class));
        cards.add(createCard("🏭", "Lieferanten",    "Lieferantendaten und Kontaktpersonen pflegen.",       SupplierView.class));
        cards.add(createCard("🛒", "Warenkorb",      "Artikel auswählen und Bestellungen aufgeben.",        ShoppingCartView.class));
        cards.add(createCard("📊", "Kontingente",    "Verfügbare Kontingente überwachen und verwalten.",    ContingentView.class));
        cards.add(createCard("📋", "Bestellhistorie","Vergangene Bestellungen einsehen und nachbestellen.", OrderView.class));
        cards.add(createCard("🗂️", "Regale",         "Regalstruktur und Lagerplätze konfigurieren.",        ShelfManagementView.class));
        cards.add(createCard("✉️", "Nachrichten",    "Eingehende Benachrichtigungen von der Logistik.",     MessageView.class));

        add(cards);
    }

    private <T extends com.vaadin.flow.component.Component> RouterLink createCard(
            String icon, String title, String description, Class<T> target) {

        Span iconSpan = new Span(icon);
        iconSpan.addClassName("home-card-icon");

        Span titleSpan = new Span(title);
        titleSpan.addClassName("home-card-title");

        Span descSpan = new Span(description);
        descSpan.addClassName("home-card-desc");

        Div card = new Div(iconSpan, titleSpan, descSpan);
        card.addClassName("home-card");

        RouterLink link = new RouterLink(target);
        link.add(card);
        link.addClassName("home-card-link");
        return link;
    }
}
