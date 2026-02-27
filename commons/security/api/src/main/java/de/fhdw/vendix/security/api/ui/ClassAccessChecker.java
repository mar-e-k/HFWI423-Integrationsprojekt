package de.fhdw.vendix.security.api.ui;

public interface ClassAccessChecker {
    boolean hasAccess(Class<?> viewClass);
}