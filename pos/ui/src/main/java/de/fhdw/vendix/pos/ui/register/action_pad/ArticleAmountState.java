package de.fhdw.vendix.pos.ui.register.action_pad;

public final class ArticleAmountState {

    private int value = 1;

    public int getValue() {
        return value;
    }

    public void append(int digit) {
        if (value == 0) {
            value = digit;
        } else {
            value = Integer.parseInt(value + "" + digit);
        }
    }

    public void clear() {
        value = 1;
    }

    public void backspace() {
        String s = String.valueOf(value);
        if (s.length() > 1) {
            value = Integer.parseInt(s.substring(0, s.length() - 1));
        } else {
            value = 1;
        }
    }
}