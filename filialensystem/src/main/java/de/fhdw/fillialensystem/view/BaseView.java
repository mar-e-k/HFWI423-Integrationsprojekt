package de.fhdw.fillialensystem.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.annotation.PostConstruct;

// BaseView ist die abstrakte Klasse, die das Basis-Layout der Seiten implementiert und von der die anderen Views erben
public abstract class BaseView extends VerticalLayout {

    public BaseView() {
        UI.getCurrent().getPage().executeJs(
                "const storedTheme = localStorage.getItem('theme');" +
                        "if (storedTheme === 'dark') {" +
                        "    document.documentElement.setAttribute('theme', 'dark');" +
                        "}"
        );

        // Top Bar vorbereiten
        setupTopBar();

        setAlignItems(Alignment.CENTER);
    }

    private void setupTopBar() {
        // Haupt-Container für die Top-Bar
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        topBar.setAlignItems(Alignment.CENTER);

        // Linker Bereich: Titel
        HorizontalLayout leftSection = new HorizontalLayout(new H1(setTopbarTitle()));
        leftSection.setJustifyContentMode(JustifyContentMode.START);
        leftSection.setWidth("33.33%");

        // Mittlerer Bereich: Optionale Buttons
        HorizontalLayout centerSection = createTopBarButtons();
        centerSection.setJustifyContentMode(JustifyContentMode.CENTER);
        centerSection.setWidth("33.33%");

        // Rechter Bereich: Uhr, Theme-Toggle, Logout
        Span liveClockLabel = new Span();
        liveClockLabel.setId("live-clock-label");
        liveClockLabel.getStyle().set("font-size", "var(--lumo-font-size-l)");
        liveClockLabel.getStyle().set("font-weight", "bold");

        Button themeToggleButton = new Button(new Icon(VaadinIcon.ADJUST), click -> {
            UI.getCurrent().getPage().executeJs("return document.documentElement.getAttribute('theme');")
                    .then(String.class, currentClientTheme -> {
                        var themeList = UI.getCurrent().getElement().getThemeList();
                        boolean isClientDark = "dark".equals(currentClientTheme);

                        if (isClientDark) {
                            themeList.remove(Lumo.DARK);
                            UI.getCurrent().getPage().executeJs("localStorage.setItem('theme', 'light');");
                            UI.getCurrent().getPage().executeJs("document.documentElement.removeAttribute('theme');");
                        } else {
                            themeList.add(Lumo.DARK);
                            UI.getCurrent().getPage().executeJs("localStorage.setItem('theme', 'dark');");
                            UI.getCurrent().getPage().executeJs("document.documentElement.setAttribute('theme', 'dark');");
                        }
                    });
        });
        themeToggleButton.setTooltipText("Toggle dark mode");

        Button logoutButton = new Button("Logout", e -> UI.getCurrent().getPage().setLocation("/logout"));

        HorizontalLayout rightSection = new HorizontalLayout(liveClockLabel, themeToggleButton, logoutButton);
        rightSection.setAlignItems(Alignment.CENTER);
        rightSection.setJustifyContentMode(JustifyContentMode.END);
        rightSection.setSpacing(true);
        rightSection.setWidth("33.33%");

        // Alle Sektionen zur Top-Bar hinzufügen
        topBar.add(leftSection, centerSection, rightSection);
        add(topBar);

        // JavaScript Live-Uhr
        UI.getCurrent().getPage().executeJs("""
            const label = document.getElementById('live-clock-label');
            if (label) {
                setInterval(() => {
                    const now = new Date();
                    label.textContent = now.toLocaleString('de-DE', {
                        year: 'numeric', month: '2-digit', day: '2-digit',
                        hour: '2-digit', minute: '2-digit', second: '2-digit'
                    });
                }, 1000);
            }
        """);
    }

    protected HorizontalLayout createTopBarButtons() {
        return new HorizontalLayout(); // Standardmäßig leer
    }

    protected abstract String setTopbarTitle();

    protected abstract void init();

    @PostConstruct
    private void postConstructInit() {
        init(); // init() wird erst nach kompletter Bean-Erstellung ausgeführt
    }
}
