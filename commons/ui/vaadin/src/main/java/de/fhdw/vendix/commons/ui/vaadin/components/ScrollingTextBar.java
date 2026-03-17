package de.fhdw.vendix.commons.ui.vaadin.components;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

@CssImport("./styles/scrolling-text.css")
public final class ScrollingTextBar extends HorizontalLayout {

    public ScrollingTextBar(String text) {
        Div wrapper = new Div();
        wrapper.addClassName("scrolling-bar-wrapper");

        Div scrollingText = new Div();
        scrollingText.setText(text);
        scrollingText.addClassName("scrolling-bar-text");

        wrapper.add(scrollingText);
        add(wrapper);

        setWidthFull();
    }
}