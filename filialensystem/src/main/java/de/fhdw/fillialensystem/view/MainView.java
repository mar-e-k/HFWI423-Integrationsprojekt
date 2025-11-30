package de.fhdw.fillialensystem.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.view.AbstractMainView;
import de.fhdw.fillialensystem.persistence.service.other.RegisterRegistryService;
import de.fhdw.fillialensystem.utility.RegisterClient;
import jakarta.annotation.security.RolesAllowed;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Route("")
@RolesAllowed({AccountRoleEnum.ROLE_ADMIN})
public class MainView extends AbstractMainView {

    private final H2 title = new H2();

    private final List<RegisterClient> kassensystemInstances;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public MainView(RegisterRegistryService registerRegistryService) {
        super();
        this.kassensystemInstances = new ArrayList<>(registerRegistryService.findAllRegistries());

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        title.setText("Connected Kassensystem Instances");

        Grid<RegisterClient> grid = createGrid();
        grid.setItems(kassensystemInstances);

        add(title, grid);


        Anchor admin = new Anchor("/admin", "Admin");
        Anchor role = new Anchor("/roles", "Rollen");
        Anchor register = new Anchor("/register", "Kassen");
        Anchor store = new Anchor("/select-store", "Filiale");
        Anchor dailyReceipt = new Anchor("/receipt-reporting", "Daily Receipt");
        add(admin, role, register, store, dailyReceipt);
    }

    private Grid<RegisterClient> createGrid() {
        Grid<RegisterClient> grid = new Grid<>(RegisterClient.class, false);

        grid.setWidthFull();

        grid.addColumn(ks -> String.format("Kasse %d (Dev, %s)", ks.getRegister().getId(), ks.getSystemClientDTO().getId()))
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

        grid.addColumn(ks -> formatter.format(ks.getRegisteredAt().atZone(ZoneId.systemDefault())))
                .setHeader("Registered At")
                .setAutoWidth(true);

        grid.addColumn(ks -> formatter.format(ks.getLastSeen().atZone(ZoneId.systemDefault())))
                .setHeader("Last Seen")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createInstanceLink)
                .setHeader("Open")
                .setAutoWidth(true);

        return grid;
    }

    private Component createOnlineBadge(RegisterClient registerClient) {
        Span badge = new Span(registerClient.isOnline() ? "Online" : "Offline");
        badge.getElement().getThemeList().add("badge");
        badge.getElement().getThemeList()
                .add(registerClient.isOnline() ? "success" : "error");
        return badge;
    }

    private Component createInstanceLink(RegisterClient registerClient) {
        String name = String.format("Kasse %d", kassensystemInstances.indexOf(registerClient) + 1);
        String url = "http://" + registerClient.getSystemClientDTO().getHost() + ":" + registerClient.getSystemClientDTO().getPort() + "/cashier";
        try {
            url += "?name=" + URLEncoder.encode(name, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // This should not happen with UTF-8
            e.printStackTrace();
        }
        Anchor link = new Anchor(url, "Open");
        link.setTarget("_blank");
        return link;
    }

    @Override
    protected void init() {

    }
}