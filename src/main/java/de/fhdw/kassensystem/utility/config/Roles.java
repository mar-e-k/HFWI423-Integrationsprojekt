package de.fhdw.kassensystem.utility.config;

public enum Roles {
    CASHIER(Type.CASHIER),
    ADMIN(Type.ADMIN);

    public static class Type {
        public static final String CASHIER = "Cashier";
        public static final String ADMIN = "Admin";
    }

    private final String label;

    Roles(String label) {
        this.label = label;
    }
}

// Kommentar von Erik: Muss später in eine Datenbank ausgelagert werden.
// Dies sollte dann aber mit den hier beschriebenen Enums übereinstimmen, da man mithilfe der Enums bessere Vergleiche / Logik etc. erstellen kann.