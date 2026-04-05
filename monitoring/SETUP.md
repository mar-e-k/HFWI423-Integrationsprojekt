# Vendix – Lasttest-Infrastruktur Setup

## Übersicht

```
docker-compose.yml              ← Prometheus + Grafana starten
prometheus.yml                  ← Prometheus-Konfiguration
grafana/
  provisioning/
    datasources/prometheus.yml  ← Prometheus-Datenquelle (automatisch)
    dashboards/dashboard.yml    ← Dashboard-Loader (automatisch)
    dashboards/*.json           ← Dein exportiertes Dashboard (→ Schritt 4)
```

---

## Schritt 1 – JMeter lokal installieren

1. Apache JMeter 5.6.3 herunterladen:
   https://jmeter.apache.org/download_jmeter.cgi → "apache-jmeter-5.6.3.zip"
2. Entpacken, z.B. nach `/opt/apache-jmeter-5.6.3` (Mac/Linux) oder `C:\jmeter` (Windows)
3. Den Pfad zur Executable notieren:
   - Mac/Linux: `/opt/apache-jmeter-5.6.3/bin/jmeter`
   - Windows:   `C:\jmeter\bin\jmeter.bat`

---

## Schritt 2 – application-dev.properties ausfüllen

```properties
vendix.performance.jmeter-bin=/opt/apache-jmeter-5.6.3/bin/jmeter

# Pfad zur JMX-Datei im Projekt (relativ oder absolut)
vendix.performance.master-jmx=/Users/DEIN_NAME/dev/vendix/vendix-lasttests.jmx

# Ergebnis-Verzeichnis (wird automatisch angelegt)
vendix.performance.results-dir=/tmp/jmeter-results

# Grafana Dashboard-UID herausfinden (→ Schritt 5)
vendix.performance.grafana-url=http://localhost:3000/d/<DASHBOARD_UID>
vendix.performance.prometheus-url=http://localhost:9090
vendix.performance.dashboard-embed-url=http://localhost:3000/d/<DASHBOARD_UID>?kiosk=true&refresh=5s
```

**JMeter-Pfad auf dem Mac schnell finden:**
```bash
which jmeter
# oder, falls über Homebrew:  brew install jmeter
# dann ist es unter:          /usr/local/bin/jmeter  oder  /opt/homebrew/bin/jmeter
```

**JMX-Pfad:** Das ist einfach der Pfad zur `vendix-lasttests.jmx` in deinem Projekt.
In IntelliJ: Rechtsklick auf die Datei → "Copy Path/Reference..." → "Absolute Path".

---

## Schritt 3 – Docker Compose starten

```bash
# Im Verzeichnis dieser docker-compose.yml:
docker compose up -d

# Läuft alles?
docker compose ps

# Logs ansehen:
docker compose logs -f
```

- Prometheus: http://localhost:9090
- Grafana:    http://localhost:3000  (Login: admin / admin)

---

## Schritt 4 – Dashboard exportieren und ins Repo legen

Damit alle Teammitglieder automatisch dasselbe Dashboard bekommen:

1. Grafana öffnen → dein Vendix-Dashboard aufrufen
2. Oben rechts: **Share** → **Export** → **Save to file**
3. Die JSON-Datei umbenennen in `vendix-lasttests.json`
4. In diesen Ordner legen:
   ```
   grafana/provisioning/dashboards/vendix-lasttests.json
   ```
5. `docker compose restart grafana`

Jetzt laden alle, die `docker compose up -d` starten, automatisch dein Dashboard.

---

## Schritt 5 – Grafana Dashboard-UID herausfinden

1. Grafana öffnen → dein Dashboard aufrufen
2. In der URL steht die UID:
   ```
   http://localhost:3000/d/abc123xyz/vendix-lasttests
                               ^^^^^^^^^^^
                               Das ist deine DASHBOARD_UID
   ```
3. Diese UID in `application-dev.properties` eintragen (→ Schritt 2)

---

## Schritt 6 – Spring Boot App starten

Ganz normal über IntelliJ oder:
```bash
./mvnw spring-boot:run -pl store -Dspring-boot.run.profiles=dev
```

Dann im Browser: http://localhost:8080 → Login → Sidebar → **Lasttests**

---

## Hinweis für Linux-Nutzer

In `prometheus.yml` ist `host.docker.internal` verwendet.
Auf Linux muss in `docker-compose.yml` unter `prometheus:` folgendes ergänzt werden:

```yaml
extra_hosts:
  - "host.docker.internal:host-gateway"
```

(Auf Mac und Windows mit Docker Desktop funktioniert es ohne diese Zeile.)