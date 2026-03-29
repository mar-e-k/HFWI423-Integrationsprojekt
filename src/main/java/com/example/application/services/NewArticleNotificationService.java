package com.example.application.services;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.Registration;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class NewArticleNotificationService {

    private final Map<UI, Consumer<Integer>> listeners = new ConcurrentHashMap<>();

    /**
     * Registriert eine UI mit einem Callback, der bei Badge-Änderungen aufgerufen wird.
     * Die zurückgegebene Registration muss in onDetach() aufgerufen werden.
     */
    public Registration register(UI ui, Consumer<Integer> onCountChanged) {
        listeners.put(ui, onCountChanged);
        return () -> listeners.remove(ui);
    }

    /**
     * Benachrichtigt alle registrierten UIs mit dem neuen Count.
     * Entfernt dabei automatisch nicht mehr aktive UIs.
     */
    public void notifyAll(int newCount) {
        listeners.entrySet().removeIf(entry -> {
            UI ui = entry.getKey();
            if (!ui.isAttached()) {
                return true; // aus Map entfernen
            }
            ui.access(() -> entry.getValue().accept(newCount));
            return false;
        });
    }
}