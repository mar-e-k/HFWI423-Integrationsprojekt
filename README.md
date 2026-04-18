# HFWI423-Integrationsprojekt

Initialisierung fuer Testing:

start-infra.bat -> startet lokale (leere) DB, pgadmin, prometheus und grafan in dieser Reihenfolge + DB-Health-Check

starte Anwendung in der IDE -> Hibernate erstellt Tabellenstruktur und fuellt sie mit ersten Daten (immer dieselben)

fuer JMeter-Test: im jmeter-test Ordner Batchjob für entsprechenden Test ausführen

stop-infra.bat -> Infrastruktur beenden

restart-infra.bat -> Infrastruktur neustarten
