# Vendix – Lasttest-Infrastruktur Setup

## Schritt 1 – Java sicherstellen

JMeter braucht zwingend eine Java-Installation mit gesetztem `JAVA_HOME`.

**Mac:** Java ist meist bereits vorhanden. Prüfen:
```bash
java -version
```

**Windows:** In der Kommandozeile prüfen:
```cmd
java -version
echo %JAVA_HOME%
```

Falls `java -version` nicht funktioniert → JDK 21 installieren: https://adoptium.net

Falls `JAVA_HOME` leer ist → Umgebungsvariable setzen:
1. Windows-Suche → "Umgebungsvariablen bearbeiten"
2. Unter "Systemvariablen" → "Neu"
3. Name: `JAVA_HOME` / Wert: z.B. `C:\Program Files\Eclipse Adoptium\jdk-21...`
4. IntelliJ neu starten

---

## Schritt 2 – JMeter lokal installieren

Apache JMeter 5.6.3 herunterladen: https://jmeter.apache.org/download_jmeter.cgi

Entpacken und den Pfad zur Executable notieren. Hier ein Beispiel:
- **Mac:** `/opt/apache-jmeter-5.6.3/bin/jmeter`
- **Windows:** `C:\jmeter\bin\jmeter.bat`

> **Windows-Hinweis:** JMeter in einen Pfad **ohne Leerzeichen und Sonderzeichen** entpacken.
> Pfade wie `C:\Users\Name\OneDrive - Firma & Co\Desktop\...` führen zu Fehlern.
> Empfehlung: `C:\jmeter\` oder `C:\tools\apache-jmeter-5.6.3\`

**Mac: Pfad schnell finden:**
```bash
which jmeter
# oder: find /Users -name "jmeter" -type f 2>/dev/null
```

---

## Schritt 3 – application-test.properties: 2 Zeilen eintragen

Nur diese zwei Werte sind individuell — alles andere funktioniert mit den Standardwerten:

**Mac/Linux:**
```properties
vendix.performance.jmeter-bin=/opt/apache-jmeter-5.6.3/bin/jmeter
vendix.performance.master-jmx=/Users/DEIN_NAME/IdeaProjects/HFWI423-Integrationsprojekt/monitoring/vendix-lasttests.jmx
```

**Windows:**
```properties
vendix.performance.jmeter-bin=C:/jmeter/bin/jmeter.bat
vendix.performance.master-jmx=C:/Users/DEIN_NAME/IdeaProjects/HFWI423-Integrationsprojekt/monitoring/vendix-lasttests.jmx
```

**JMX-Pfad in IntelliJ finden:** Rechtsklick auf `monitoring/vendix-lasttests.jmx` → Copy Path → Absolute Path

> **Windows-Hinweis:** Vorwärts-Slashes (`/`) funktionieren in Properties-Dateien auf Windows
> genauso wie Backslashes — kein manuelles Ersetzen nötig.

---

## Schritt 4 – JMeter-Test-Account

Im Store-UI wurde ein JMeter-Admin-Account bereits angelegt:
- **Username:** `JMeter` / **Passwort:** `1234`

Diesen Account **nie im Browser verwenden** — nur JMeter darf sich damit einloggen.

---

## Schritt 5 – Docker Compose starten

```bash
cd monitoring/
docker compose up -d
```

- Prometheus: http://localhost:9090
- Grafana:    http://localhost:3000  (admin / admin)

Das Dashboard lädt automatisch.

---

## Schritt 6 – Spring Boot starten und loslegen

Über IntelliJ starten, dann: `http://localhost:8080` → Login → Sidebar → **Lasttests**

---

## Testparameter anpassen (User-Anzahl / Dauer)

`monitoring/vendix-lasttests.jmx` als Text öffnen (IntelliJ: Rechtsklick → Open As → Text).
Pro ThreadGroup die gewünschten Werte anpassen:

```xml
<intProp name="ThreadGroup.num_threads">30</intProp>    ← Anzahl gleichzeitiger User
<intProp name="ThreadGroup.ramp_time">60</intProp>      ← Sekunden bis alle User aktiv sind
<longProp name="ThreadGroup.duration">300</longProp>    ← Testdauer in Sekunden
```