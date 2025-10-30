# HFWI423-Integrationsprojekt

Dieses Projekt ist ein Kassensystem, das mit Vaadin und Spring Boot entwickelt wird.

## 1. Build und Start der Anwendung

### 1.1. Projekt bauen (Kompilieren)

Bevor Sie die Anwendung zum ersten Mal starten oder nachdem Sie größere Änderungen an den Abhängigkeiten (`pom.xml`) vorgenommen haben, sollten Sie das Projekt mit Maven bauen. Dieser Befehl lädt alle Notwendigkeiten herunter und stellt sicher, dass das Projekt fehlerfrei kompiliert.

Öffnen Sie ein Terminal im Projektverzeichnis und führen Sie aus:

```sh
mvn clean install
```

In IntelliJ können Sie hierfür auch das **Maven-Tool-Fenster** verwenden, zu `Lifecycle` navigieren und dort zuerst auf `clean` und dann auf `install` doppelklicken.

### 1.2. Anwendung starten

Der empfohlene Weg zum Starten der Anwendung während der Entwicklung ist direkt über die IDE. Dies ist besonders nützlich für das Debugging.

1.  Öffnen Sie die Datei `src/main/java/de/fhdw/kassensystem/Application.java`.
2.  Klicken Sie auf den grünen "Play"-Button neben der `main`-Methode.

Die Anwendung startet und ist unter `http://localhost:8080` erreichbar.

**Wichtig:** Damit der Live-Reload bei diesem Startmodus funktioniert, müssen die IDE-Einstellungen wie in Abschnitt 3 beschrieben konfiguriert werden.

## 2. Projektübersicht

Das Projekt ist eine Spring-Boot-Anwendung, die ein Kassensystem implementiert.

*   **Backend:** Spring Boot, Spring Data JPA, Spring Security
*   **Datenbank:** PostgreSQL
*   **Frontend:** Vaadin Flow (Java-basierte UI-Komponenten)

### Bisherige Funktionalität:

*   **Login-Ansicht:** Eine einfache Anmeldemaske (`LoginView.java`).
*   **Hauptansicht:** Eine Basis-Ansicht (`MainView.java`), die nach dem Login erscheint.
*   **Kassenansicht:** Eine geschützte Ansicht (`CashierView.java`), die nur für Benutzer mit der Rolle "CASHIER" zugänglich ist.
*   **Sicherheit:** Spring Security ist grundlegend konfiguriert, um die Ansichten zu schützen.

## 3. Live-Reload für die Entwicklung aktivieren

Live-Reload ermöglicht es, Änderungen am Java- und Frontend-Code sofort im Browser zu sehen, ohne die Anwendung manuell neu starten zu müssen. Damit dies funktioniert, sind die folgenden IDE-Einstellungen (IntelliJ IDEA) erforderlich:

1.  **Automatisches Bauen aktivieren:**
    *   Gehen Sie zu `Settings/Preferences > Build, Execution, Deployment > Compiler`.
    *   Aktivieren Sie die Option **`Build project automatically`**.

2.  **Automatisches Bauen während der Ausführung erlauben:**
    *   Gehen Sie zu `Settings/Preferences > Advanced Settings`.
    *   Suchen Sie die Option **`Allow auto-make to start even if developed application is currently running`** und aktivieren Sie sie.

Nachdem diese Einstellungen vorgenommen wurden, funktioniert der Live-Reload beim Start über die `Application.java`.
