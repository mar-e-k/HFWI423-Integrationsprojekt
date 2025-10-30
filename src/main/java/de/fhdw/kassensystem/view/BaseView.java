package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.Lumo;

//BaseView ist die abstrakte Klasse, die das Basis-Layout der Seiten implementiert und von der die anderen Views erben
public abstract class BaseView extends VerticalLayout {

    public BaseView() {
        // Top Bar
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topBar.setAlignItems(FlexComponent.Alignment.CENTER);

        // Titel wird von der Subklasse geholt
        H1 viewTitle = new H1(getViewTitle());

        // Live-Uhr und Datumsanzeige
        Span liveClockLabel = new Span();
        liveClockLabel.setId("live-clock-label"); // ID für JavaScript-Zugriff
        liveClockLabel.getStyle().set("font-size", "var(--lumo-font-size-l)"); // Etwas größer
        liveClockLabel.getStyle().set("font-weight", "bold"); // Fettgedruckt

        // Dark Mode Button
        Button themeToggleButton = new Button(new Icon(VaadinIcon.ADJUST), click -> {
            var themeList = UI.getCurrent().getElement().getThemeList();
            if (themeList.contains(Lumo.DARK)) {
                themeList.remove(Lumo.DARK);
            } else {
                themeList.add(Lumo.DARK);
            }
        });
        themeToggleButton.setTooltipText("Toggle dark mode");

        // Logout Button
        Button logoutButton = new Button("Logout", e -> UI.getCurrent().getPage().setLocation("/logout"));

        // Rechte Seite der Top Bar: Uhr, Toggle, Logout
        HorizontalLayout rightSide = new HorizontalLayout(liveClockLabel, themeToggleButton, logoutButton);
        rightSide.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSide.setSpacing(true);

        topBar.add(viewTitle, rightSide);

        add(topBar);

        // JavaScript für die Live-Uhr
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

        // Subklasse fügt ihren spezifischen Inhalt hinzu
        initView();

        setAlignItems(FlexComponent.Alignment.CENTER);
    }

     // Subklassen müssen diese Methode implementieren, um den Titel für die Ansicht zu erstellen.

    protected abstract String getViewTitle();

    // Subklassen müssen diese Methode implementieren, um ihre spezifische UI hinzuzufügen.

    protected abstract void initView();
}