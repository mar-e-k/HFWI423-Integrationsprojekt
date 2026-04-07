package de.fhdw.vendix.commons.spring.vaadin.utility;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateTimeFormat {

    public static final DateTimeFormatter UI_DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", Locale.GERMANY);
    public static final DateTimeFormatter UI_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMANY);
    public static final DateTimeFormatter UI_TIME = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.GERMANY);

    private DateTimeFormat() {}
}