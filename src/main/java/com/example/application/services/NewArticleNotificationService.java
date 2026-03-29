package com.example.application.services;

import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NewArticleNotificationService {

    private final Set<UI> registeredUis = ConcurrentHashMap.newKeySet();

    public void register(UI ui) {
        if (ui != null) {
            registeredUis.add(ui);
        }
    }

    public void unregister(UI ui) {
        if (ui != null) {
            registeredUis.remove(ui);
        }
    }

    public void notifyBadgeChanged() {
        registeredUis.removeIf(ui -> ui == null || !ui.isAttached());

        for (UI ui : registeredUis) {
            ui.access(() -> ui.getPage().executeJs(
                    "window.dispatchEvent(new CustomEvent('badge-refresh'))"));
        }
    }
}