package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
    }

    private void createHeader() {
        // App title
        H1 title = new H1("Einkaufssystem");
        title.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE
        );

        // Navigation links
        RouterLink homeLink = new RouterLink("Home", HomeView.class);
        RouterLink articlesLink = new RouterLink("Artikel", ArticleView.class);
        RouterLink suppliersLink = new RouterLink("Lieferanten", SupplierView.class);

        // Highlight active route
        homeLink.setHighlightCondition(HighlightConditions.sameLocation());
        articlesLink.setHighlightCondition(HighlightConditions.sameLocation());
        suppliersLink.setHighlightCondition(HighlightConditions.sameLocation());

        // Navigation layout
        HorizontalLayout navLinks = new HorizontalLayout(homeLink, articlesLink, suppliersLink);
        navLinks.setSpacing(true);
        navLinks.setPadding(true);
        navLinks.setAlignItems(FlexComponent.Alignment.CENTER);
        navLinks.addClassNames(
                LumoUtility.Gap.MEDIUM,
                LumoUtility.Margin.NONE,
                LumoUtility.Padding.Vertical.SMALL
        );

        // Top bar layout
        HorizontalLayout header = new HorizontalLayout(title, navLinks);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.Padding.Horizontal.LARGE,
                LumoUtility.Padding.Vertical.SMALL
        );

        addToNavbar(header);
    }
}
