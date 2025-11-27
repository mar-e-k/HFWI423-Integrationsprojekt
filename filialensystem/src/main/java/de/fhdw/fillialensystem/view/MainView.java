package de.fhdw.fillialensystem.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.view.AbstractMainView;
import de.fhdw.fillialensystem.api.registry.KassensystemInstance;
import de.fhdw.fillialensystem.api.registry.KassensystemRegistryService;
import jakarta.annotation.security.RolesAllowed;

@Route("")
@RolesAllowed({AccountRoleEnum.ROLE_ADMIN})
public class MainView extends AbstractMainView {

    public MainView(KassensystemRegistryService kassensystemRegistryService) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Connected Kassensystem Instances");
        add(title);

        Grid<KassensystemInstance> grid = createGrid();
        grid.setItems(kassensystemRegistryService.findAllRegistries());

        add(grid);
    }

    private Grid<KassensystemInstance> createGrid() {
        Grid<KassensystemInstance> grid = new Grid<>(KassensystemInstance.class, false);

        grid.setWidthFull();

        grid.addColumn(ks -> ks.getSystemClientDTO().getId())
                .setHeader("Device")
                .setAutoWidth(true);

        grid.addColumn(ks -> ks.getSystemClientDTO().getHost())
                .setHeader("Host")
                .setAutoWidth(true);

        grid.addColumn(ks -> ks.getSystemClientDTO().getPort())
                .setHeader("Port")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createOnlineBadge)
                .setHeader("Status")
                .setAutoWidth(true);

        grid.addColumn(ks -> ks.getRegisteredAt().toString())
                .setHeader("Registered At")
                .setAutoWidth(true);

        grid.addColumn(ks -> ks.getLastSeen().toString())
                .setHeader("Last Seen")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createInstanceLink)
                .setHeader("Open")
                .setAutoWidth(true);

        return grid;
    }

    private Component createOnlineBadge(KassensystemInstance ks) {
        Span badge = new Span(ks.isOnline() ? "Online" : "Offline");
        badge.getElement().getThemeList().add("badge");
        badge.getElement().getThemeList()
                .add(ks.isOnline() ? "success" : "error");
        return badge;
    }

    private Component createInstanceLink(KassensystemInstance ks) {
        String url = "http://" + ks.getSystemClientDTO().getHost() + ":" + ks.getSystemClientDTO().getPort();
        Anchor link = new Anchor(url, "Open");
        link.setTarget("_blank");
        return link;
    }

    @Override
    protected void init() {

    }
}