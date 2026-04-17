// ═══════════════════════════════════════════════════════════════════════════
// lib/config.js – Zentrale Konfiguration für alle k6-Lasttests
// ═══════════════════════════════════════════════════════════════════════════

// Orchestrator und Store-Hosts
// Standardmäßig: Spring Boot läuft auf dem Host → "host.docker.internal"
// Kann via ENV überschrieben werden: -e ORCHESTRATOR_URL=... -e STORE_URL=...
export const ORCHESTRATOR_URL = __ENV.ORCHESTRATOR_URL || 'http://host.docker.internal:8080';
export const STORE_URL        = __ENV.STORE_URL        || 'http://host.docker.internal:8081';

function parseIdList(envValue, fallback) {
    if (!envValue) {
        return fallback;
    }

    const parsed = envValue
        .split(',')
        .map((value) => Number(value.trim()))
        .filter((value) => Number.isInteger(value) && value > 0);

    return parsed.length > 0 ? parsed : fallback;
}

// Login-Credentials für Test-Account (wird im Setup einmalig ausgetauscht gegen JWT)
export const ADMIN_USERNAME = __ENV.ADMIN_USERNAME || 'admin.three';
export const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || 'admin';

// Store und Kassen-IDs (entsprechen dem DB-Setup)
export const STORE_ID     = Number(__ENV.STORE_ID || 1);
export const REGISTER_IDS = parseIdList(__ENV.REGISTER_IDS, [1, 2, 3]);

// Kassierer-Pool für Rotation (IDs entsprechen cashier.one/two/three aus DB-Setup)
export const CASHIER_IDS = parseIdList(__ENV.CASHIER_IDS, [4, 5, 6]);

// Artikel-Pool (A-0001 bis A-0100 sind normale Artikel, 101/102 sind Pfand-Artikel)
export const NORMAL_ARTICLE_IDS  = Array.from({ length: 100 }, (_, i) => i + 1);
export const DEPOSIT_ARTICLE_IDS = [101, 102];

// Rabatt-Palette (50 verschiedene Möglichkeiten für den Spike-Test)
// Besteht aus 5% bis 50% in 1%-Schritten + einige Rundbeträge
export const DISCOUNT_RATES = Array.from({ length: 46 }, (_, i) => 5 + i);
// ergibt: 5, 6, 7, ..., 50 (=46 Rabattstufen)
// Plus klassische Werte für Lasttest (10% und 30%)
export const LASTTEST_DISCOUNTS = [10, 30];

// Zahlungsmethoden
export const PAYMENT_METHODS = ['CASH', 'CARD'];

// HTTP-Defaults für k6
export const HTTP_PARAMS_JSON = {
    headers: {
        'Content-Type': 'application/json',
        'Accept':       'application/json',
    },
    timeout: '10s',
};

// Aktuell laufendes Szenario (für Logging/Debugging)
export const CURRENT_SCENARIO = __ENV.SCENARIO || 'lasttest';
