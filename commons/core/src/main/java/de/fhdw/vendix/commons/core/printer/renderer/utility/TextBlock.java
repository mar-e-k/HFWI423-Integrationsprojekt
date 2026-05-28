package de.fhdw.vendix.commons.core.printer.renderer.utility;

public final class TextBlock {

    private String text;
    private TextStyle style;

    public TextBlock(String text, TextStyle style) {
        if (text == null) {
            throw new IllegalArgumentException("Parameter 'text' cannot be null");
        }
        if (style == null) {
            throw new IllegalArgumentException("Parameter 'style' cannot be null");
        }
        this.text = text;
        this.style = style;
    }

    public static TextBlock empty() {
        return new TextBlock(
                "",
                TextStyle.defaultStyle()
        );
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Parameter 'text' cannot be null");
        }
        this.text = text;
    }

    public TextStyle getStyle() {
        return style;
    }

    public void setStyle(TextStyle style) {
        if (style == null) {
            throw new IllegalArgumentException("Parameter 'style' cannot be null");
        }
        this.style = style;
    }
}