package de.fhdw.vendix.commons.ui.layout;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Sidebar extends VerticalLayout {

    private final Map<SidebarEntry, RouterLink> links = new LinkedHashMap<>();

    public Sidebar() {
        configureLayout();
    }

    public Sidebar(List<SidebarEntry> entries) {
        configureLayout();
        entries.forEach(this::addLink);
    }

    private void configureLayout() {
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.STRETCH);
    }

    private static RouterLink createLink(SidebarEntry entry) {
        Icon icon = new Icon(entry.icon());
        Span text = new Span(entry.label());
        text.getStyle().set("margin-left", "var(--lumo-space-m)");

        RouterLink link = new RouterLink(entry.target());
        link.add(icon, text);

        link.addClassName("sidebar-link");

        return link;
    }

    public void addLink(SidebarEntry entry) {
        if (links.containsKey(entry)) {
            return;
        }

        RouterLink link = createLink(entry);
        links.put(entry, link);
        add(link);
    }

    public void removeLink(SidebarEntry entry) {
        RouterLink link = links.remove(entry);
        if (link != null) {
            remove(link);
        }
    }
}