package de.fhdw.vendix.store.persistence.service.performance;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Konfiguration für den Performance-Test-Service.
 *
 * Jeder Entwickler muss nur diese 2 Properties in seiner application-test.properties setzen:
 *
 *   vendix.performance.jmeter-bin=/Users/DEIN_NAME/Desktop/apache-jmeter-5.6.3/bin/jmeter
 *   vendix.performance.master-jmx=/Users/DEIN_NAME/IdeaProjects/HFWI423-Integrationsprojekt/monitoring/vendix-lasttests.jmx
 *
 * Alle anderen Werte (Grafana, Prometheus, results-dir) funktionieren mit den
 * eingetragenen Standardwerten ohne weitere Konfiguration.
 */
@Component
@ConfigurationProperties(prefix = "vendix.performance")
public class PerformanceTestProperties {

    /** Pfad zur JMeter-Executable — MUSS individuell gesetzt werden */
    private String jmeterBin = "jmeter";

    /** Pfad zur Master-JMX-Datei — MUSS individuell gesetzt werden */
    private String masterJmx;

    /** Ergebnis-Verzeichnis — Standard funktioniert auf Mac/Linux/Windows ohne Anpassung */
    private String resultsDir = buildDefaultResultsDir();

    private static String buildDefaultResultsDir() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        // java.io.tmpdir gibt auf Windows den Pfad mit abschließendem \ zurück
        // → ohne diesen Check entsteht ein doppelter Separator: C:\Temp\\jmeter-results
        if (tmpDir.endsWith(java.io.File.separator) || tmpDir.endsWith("/")) {
            tmpDir = tmpDir.substring(0, tmpDir.length() - 1);
        }
        return tmpDir + java.io.File.separator + "jmeter-results";
    }

    /** Grafana-Dashboard-Link — Standard passt wenn Docker Compose läuft */
    private String grafanaUrl = "http://localhost:3000/d/adf44rg";

    /** Prometheus-Link — Standard passt wenn Docker Compose läuft */
    private String prometheusUrl = "http://localhost:9090";

    /** Grafana IFrame URL — Standard passt, zeigt letzte 30 Minuten */
    private String dashboardEmbedUrl =
            "http://localhost:3000/d/adf44rg?kiosk=true&refresh=5s&from=now-30m&to=now";

    // --- Getters & Setters ---

    public String getJmeterBin() { return jmeterBin; }
    public void setJmeterBin(String jmeterBin) { this.jmeterBin = jmeterBin; }

    public String getMasterJmx() { return masterJmx; }
    public void setMasterJmx(String masterJmx) { this.masterJmx = masterJmx; }

    public String getResultsDir() { return resultsDir; }
    public void setResultsDir(String resultsDir) { this.resultsDir = resultsDir; }

    public String getGrafanaUrl() { return grafanaUrl; }
    public void setGrafanaUrl(String grafanaUrl) { this.grafanaUrl = grafanaUrl; }

    public String getPrometheusUrl() { return prometheusUrl; }
    public void setPrometheusUrl(String prometheusUrl) { this.prometheusUrl = prometheusUrl; }

    public String getDashboardEmbedUrl() { return dashboardEmbedUrl; }
    public void setDashboardEmbedUrl(String dashboardEmbedUrl) { this.dashboardEmbedUrl = dashboardEmbedUrl; }
}