# HFWI423-Integrationsprojekt: Filialen- und Kassensystem

Dieses Projekt implementiert ein verteiltes System, bestehend aus einem zentralen **Filialensystem** und mehreren dezentralen **Kassensystemen**. Die Kommunikation erfolgt über REST-Schnittstellen.

## 1. Projektarchitektur

Das System ist in drei Maven-Module unterteilt:

-   **`commons`**: Eine Bibliothek, die von beiden anderen Modulen genutzt wird. Sie enthält gemeinsame Code-Bestandteile wie Datenübertragungsobjekte (DTOs), Entitätsklassen und Basis-UI-Komponenten.
-   **`filialensystem`**: Die zentrale Verwaltungsanwendung. Sie dient als "Single Source of Truth" für Artikeldaten und verwaltet die angeschlossenen Kassensysteme.
-   **`kassensystem`**: Die Anwendung für den Point of Sale. Mehrere Instanzen dieses Systems können gestartet werden. Jede Instanz registriert sich beim Filialensystem, um Artikeldaten abzurufen und Verkäufe abzuwickeln.

## 2. Build und Start der Anwendungen

### 2.1. Projekt bauen (Kompilieren)

Bevor Sie die Anwendungen zum ersten Mal starten, muss das gesamte Projekt mit Maven gebaut werden. Dieser Befehl kompiliert alle drei Module und stellt sicher, dass die Abhängigkeiten korrekt aufgelöst werden.

Öffnen Sie ein Terminal im Projekt-Hauptverzeichnis und führen Sie aus:

```sh
mvn clean install
```

### 2.2. Anwendungen starten

Für einen funktionsfähigen Betrieb müssen sowohl das Filialensystem als auch mindestens ein Kassensystem gestartet werden.

#### 1. Filialensystem starten

Das Filialensystem ist die zentrale Verwaltungsinstanz und läuft standardmäßig auf Port `8080`.

1.  Öffnen Sie die Datei `filialensystem/src/main/java/de/fhdw/fillialensystem/Application.java`.
2.  Klicken Sie auf den grünen "Play"-Button neben der `main`-Methode, um die Anwendung zu starten.
3.  Die Anwendung ist unter `http://localhost:8080` erreichbar.

**Standard-Login:**
-   **Benutzername:** `A`
-   **Passwort:** `1234`

#### 2. Kassensystem starten

Das Kassensystem ist die Point-of-Sale-Anwendung. Es kann mehrfach gestartet werden und läuft standardmäßig auf Port `8081`.

1.  Öffnen Sie die Datei `kassensystem/src/main/java/de/fhdw/kassensystem/Application.java`.
2.  Klicken Sie auf den grünen "Play"-Button neben der `main`-Methode.
3.  Die Anwendung ist unter `http://localhost:8081` erreichbar.

**Standard-Login:**
-   **Benutzername:** `C`
-   **Passwort:** `1234`

Nach dem Start registriert sich das Kassensystem automatisch beim Filialensystem.

## 3. Funktionsübersicht

### Filialensystem (`:8080`)

-   **Dashboard (`MainView`):**
    -   Zeigt eine Übersicht aller verbundenen Kassensystem-Instanzen.
    -   Stellt den Online-Status, Host, Port und den Zeitpunkt der letzten Kommunikation dar.
    -   Bietet einen direkten Link, um die `CashierView` der jeweiligen Kasseninstanz in einem neuen Tab zu öffnen. Der Name der Kasse (z.B. "Kasse 1") wird dabei als URL-Parameter übergeben.
-   **Artikelverwaltung (`AdminView`):**
    -   Anzeige aller im System erfassten Artikel in einer Tabelle.
    -   Suchfunktion zum Filtern der Artikelliste.
    -   Möglichkeit, neue Artikel zu erstellen und bestehende zu bearbeiten.

### Kassensystem (`:8081`)

-   **Kassenansicht (`CashierView`):**
    -   Nimmt den übergebenen Kassennamen aus der URL entgegen und zeigt ihn im Titel an (z.B. "CashierView der Kasse 1"). Der Name bleibt auch nach einem Logout und erneutem Login erhalten.
    -   **Artikelsuche:** Artikel können über ihre Artikelnummer gesucht und dem Warenkorb hinzugefügt werden.
    -   **Warenkorb-Management:**
        -   Artikel ohne vordefinierten Preis erfordern eine Preiseingabe durch den Kassierer.
        -   Mengen und Preise können direkt im Warenkorb bearbeitet werden. Eine Preisänderung erfordert eine Passwort-Freigabe (`Initial: 1234`).
        -   Artikel können vollständig oder in Teilmengen aus dem Warenkorb entfernt werden.
        -   Rabatte können pro Position (prozentual, für eine bestimmte Menge) hinzugefügt werden.
    -   **Kaufabschluss:** Führt zur Bezahlansicht (`PaymentView`).

## 4. Live-Reload für die Entwicklung aktivieren

Live-Reload ermöglicht es, Änderungen am Code sofort im Browser zu sehen, ohne die Anwendung manuell neu starten zu müssen. Damit dies funktioniert, sind die folgenden IDE-Einstellungen (IntelliJ IDEA) erforderlich:

1.  **Automatisches Bauen aktivieren:**
    -   Gehen Sie zu `Settings/Preferences > Build, Execution, Deployment > Compiler`.
    -   Aktivieren Sie die Option **`Build project automatically`**.

2.  **Automatisches Bauen während der Ausführung erlauben:**
    -   Gehen Sie zu `Settings/Preferences > Advanced Settings`.
    -   Suchen Sie die Option **`Allow auto-make to start even if developed application is currently running`** und aktivieren Sie sie.

Nachdem diese Einstellungen vorgenommen wurden, funktioniert der Live-Reload beim Start über die jeweilige `Application.java`.
