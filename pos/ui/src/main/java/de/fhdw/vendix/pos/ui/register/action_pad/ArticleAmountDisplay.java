package de.fhdw.vendix.pos.ui.register.action_pad;

import com.vaadin.flow.component.html.Span;

public final class ArticleAmountDisplay extends Span {

    public ArticleAmountDisplay() {
        getStyle()
                .set("font-size", "2em")
                .set("font-weight", "bold");
    }

    public void setValue(int value) {
        setText(String.valueOf(value));
    }
}