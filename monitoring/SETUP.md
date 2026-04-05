# Vendix – Lasttest-Infrastruktur Setup

## Schritt 1 – JMeter lokal installieren

Apache JMeter 5.6.3 herunterladen: https://jmeter.apache.org/download_jmeter.cgi

Entpacken und den Pfad zur Executable notieren:
- Mac/Linux: `/opt/apache-jmeter-5.6.3/bin/jmeter`
- Windows: `C:\jmeter\bin\jmeter.bat`

```bash
# Mac: Pfad schnell finden
which jmeter
# oder: find /Users -name "jmeter" -type f 2>/dev/null
```

---

## Schritt 2 – application-test.properties: 2 Zeilen eintragen

Nur diese zwei Werte sind individuell — alles andere funktioniert mit den Standardwerten:

```properties
vendix.performance.jmeter-bin=/Users/DEIN_NAME/Desktop/apache-jmeter-5.6.3/bin/jmeter
vendix.performance.master-jmx=/Users/DEIN_NAME/IdeaProjects/HFWI423-Integrationsprojekt/monitoring/vendix-lasttests.jmx
```

**JMX-Pfad in IntelliJ finden:** Rechtsklick auf `monitoring/vendix-lasttests.jmx` → Copy Path → Absolute Path

---

## Schritt 3 – JMeter-Test-Account wurde bereits angelegt

Im Store-UI wurde ein JMeter-Admin-Account angelegt:
- **Username:** `JMeter` / **Passwort:** `1234`

Diesen Account **nie im Browser verwenden** — nur JMeter darf sich damit einloggen und validieren, ob der in der DB existiert.

---

## Schritt 4 – Docker Compose starten

```bash
cd monitoring/
docker compose up -d
```

- Prometheus: http://localhost:9090
- Grafana:    http://localhost:3000  (admin / admin)

Das Dashboard lädt automatisch.

---

## Schritt 5 – Spring Boot starten und loslegen

Über IntelliJ starten, dann: `http://localhost:8080` → Login → Sidebar → **Lasttests**

---

## Testparameter anpassen (User-Anzahl / Dauer)

`monitoring/vendix-lasttests.jmx` als Text öffnen, pro ThreadGroup:

```xml
<stringProp name="ThreadGroup.num_threads">10</stringProp>   ← User-Anzahl
<stringProp name="ThreadGroup.ramp_time">60</stringProp>     ← Ramp-up in Sekunden
<stringProp name="ThreadGroup.duration">180</stringProp>     ← Testdauer in Sekunden
```

---

## Hinweis für Linux-Nutzer

In `docker-compose.yml` unter `prometheus:` ergänzen:

```yaml
extra_hosts:
  - "host.docker.internal:host-gateway"
```