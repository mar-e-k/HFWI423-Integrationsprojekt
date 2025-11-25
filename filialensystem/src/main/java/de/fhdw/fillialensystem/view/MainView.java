package de.fhdw.fillialensystem.view;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.fillialensystem.api.registry.KassensystemInstance;
import de.fhdw.fillialensystem.api.registry.KassensystemRegistryService;
import jakarta.annotation.security.PermitAll;

@Route("")
@PermitAll
public class MainView extends VerticalLayout {

    private final KassensystemRegistryService kassensystemRegistryService;

    public MainView(KassensystemRegistryService kassensystemRegistryService) {
        this.kassensystemRegistryService = kassensystemRegistryService;
        init();
    }

    private void init() {
        for (KassensystemInstance kassensystemInstance : kassensystemRegistryService.findAllRegistries()) {
            add(new Span("%s@%s:%d_%s".formatted(
                    kassensystemInstance.getSystemClientDTO().getId(),
                    kassensystemInstance.getSystemClientDTO().getHost(),
                    kassensystemInstance.getSystemClientDTO().getPort(),
                    kassensystemInstance.isOnline()
            )));
        }
    }
}