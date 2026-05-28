package de.fhdw.vendix.orchestrator.ui.performance;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.orchestrator.ui.OrchestratorAppLayout;
import jakarta.annotation.security.RolesAllowed;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

@Route(value = "lasttests", layout = OrchestratorAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
public class LasttestsView extends VerticalLayout {

    private static final DateTimeFormatter STATUS_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    private final PerformanceTestLauncher launcher;
    private final Grid<PerformanceTestStatus> statusGrid = new Grid<>(PerformanceTestStatus.class, false);
    private final Map<PerformanceTestType, Button> buttons = new EnumMap<>(PerformanceTestType.class);

    public LasttestsView(PerformanceTestLauncher launcher) {
        this.launcher = launcher;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(
                new H2("Lasttests"),
                new Paragraph("Die Buttons starten die hinterlegten K6-Szenarien direkt aus der Orchestrator-UI.")
        );
        add(createActionBar());

        for (PerformanceTestType type : PerformanceTestType.values()) {
            add(createTestRow(type));
        }

        configureStatusGrid();
        add(statusGrid);
        refreshStatuses();
    }

    private Component createActionBar() {
        Button refreshButton = new Button("Status aktualisieren");
        refreshButton.addClickListener(event -> refreshStatuses());

        HorizontalLayout actionBar = new HorizontalLayout(refreshButton);
        actionBar.setWidthFull();
        actionBar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return actionBar;
    }

    private Component createTestRow(PerformanceTestType type) {
        H3 title = new H3(type.label());
        title.getStyle().setMargin("0");

        Paragraph description = new Paragraph(type.description());
        description.getStyle().setMargin("0");

        VerticalLayout textBlock = new VerticalLayout(title, description);
        textBlock.setPadding(false);
        textBlock.setSpacing(false);

        Button startButton = new Button("Starten");
        startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        startButton.addClickListener(event -> launch(type));
        buttons.put(type, startButton);

        HorizontalLayout row = new HorizontalLayout(textBlock, startButton);
        row.setWidthFull();
        row.setAlignItems(Alignment.CENTER);
        row.setJustifyContentMode(JustifyContentMode.BETWEEN);
        row.getStyle().setBorder("1px solid var(--lumo-contrast-20pct)");
        row.getStyle().setBorderRadius("var(--lumo-border-radius-l)");
        row.getStyle().setPadding("var(--lumo-space-m)");
        return row;
    }

    private void configureStatusGrid() {
        statusGrid.addColumn(status -> status.type().label()).setHeader("Test");
        statusGrid.addColumn(PerformanceTestStatus::state).setHeader("Status");
        statusGrid.addColumn(status -> formatStartedAt(status.startedAt()))
                .setHeader("Gestartet");
        statusGrid.addColumn(status -> formatExitCode(status.exitCode()))
                .setHeader("Exit-Code");
        statusGrid.addColumn(status -> formatLogFile(status.logFile()))
                .setHeader("Logdatei")
                .setAutoWidth(true)
                .setFlexGrow(1);
        statusGrid.setWidthFull();
    }

    private static String formatStartedAt(@Nullable Instant startedAt) {
        return startedAt == null ? "-" : STATUS_TIME_FORMAT.format(startedAt);
    }

    private static String formatExitCode(@Nullable Integer exitCode) {
        return exitCode == null ? "-" : exitCode.toString();
    }

    private static String formatLogFile(@Nullable Path logFile) {
        return logFile == null ? "-" : logFile.toString();
    }

    private void launch(PerformanceTestType type) {
        PerformanceTestLaunchResult result = launcher.launch(type);
        Notification.show(result.message(), 5000, Position.BOTTOM_START);
        refreshStatuses();
    }

    private void refreshStatuses() {
        statusGrid.setItems(launcher.statuses());
        launcher.statuses().forEach(status -> {
            Button button = buttons.get(status.type());
            if (button != null) {
                button.setEnabled(!status.running());
                button.setText(status.running() ? "Laeuft" : "Starten");
            }
        });
    }
}
