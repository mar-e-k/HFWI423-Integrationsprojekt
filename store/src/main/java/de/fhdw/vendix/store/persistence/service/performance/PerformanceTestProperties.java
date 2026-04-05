package de.fhdw.vendix.store.persistence.service.performance;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Konfiguration für den Performance-Test-Service.
 * Werte werden aus application.properties unter dem Präfix "vendix.performance" gelesen.
 *
 * Beispiel in application-dev.properties:
 *
 *   vendix.performance.jmeter-bin=/opt/apache-jmeter/bin/jmeter
 *   vendix.performance.master-jmx=/path/to/vendix-lasttests.jmx
 *   vendix.performance.results-dir=/tmp/jmeter-results
 *   vendix.performance.grafana-url=http://localhost:3000/d/<DASHBOARD_ID>
 *   vendix.performance.prometheus-url=http://localhost:9090
 *   vendix.performance.dashboard-embed-url=http://localhost:3000/d/<DASHBOARD_ID>?kiosk&refresh=5s
 */
@Component
@ConfigurationProperties(prefix = "vendix.performance")
public class PerformanceTestProperties {

    /** Pfad zur JMeter-Executable, z.B. /opt/jmeter/bin/jmeter */
    private String jmeterBin = "jmeter";

    /** Pfad zur Master-JMX-Datei, die alle 5 Thread Groups enthält */
    private String masterJmx;

    /** Verzeichnis, in das JMeter die CSV-Ergebnisse schreibt */
    private String resultsDir = System.getProperty("java.io.tmpdir") + "/jmeter-results";

    /** URL des Grafana-Dashboards (als Link) */
    private String grafanaUrl = "http://localhost:3000";

    /** URL von Prometheus */
    private String prometheusUrl = "http://localhost:9090";

    /** URL für das eingebettete Grafana-Dashboard (kiosk-Modus) */
    private String dashboardEmbedUrl = "http://localhost:3000";

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
