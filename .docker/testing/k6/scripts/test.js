// ═══════════════════════════════════════════════════════════════════════════
// test.js – Vendix Kassensystem Lasttests (5 Szenarien)
//
// Ausführung im Docker-Container:
//   docker exec vendix-k6 k6 run -o experimental-prometheus-rw \
//     /etc/k6/scripts/test.js -e SCENARIO=lasttest
//
// Verfügbare Szenarien:
//   lasttest      – 3 Kassen, 30 Trans/h, 18 Art/Bon, 30 min (Normalbetrieb)
//   stresstest    – 10 → 150 VUs stufenweise, 1 Bon/3s, 18 min (Überlastung)
//   spiketest     – 3 → 20 Kassen Spitze, 70-80% Rabatte, ~30 min
//   soaktest      – Tagesverlauf 06-22 Uhr über 16 h (Langzeit)
//   capacitytest  – Multi-dimensionaler Stufentest, ~20 min (Kipppunkt finden)
// ═══════════════════════════════════════════════════════════════════════════

import { sleep } from 'k6';
import exec from 'k6/execution';

import { CURRENT_SCENARIO, LASTTEST_DISCOUNTS, DISCOUNT_RATES } from './lib/config.js';
import { setupAuth, getCashierId } from './lib/auth.js';
import { runFullBon, createReceipt, addArticleLine, pickRegisterId, pickPaymentMethod, pickArticleId } from './lib/business.js';

// ═══════════════════════════════════════════════════════════════════════════
// SETUP – läuft 1x vor allen VUs
// ═══════════════════════════════════════════════════════════════════════════

export function setup() {
    console.log('═'.repeat(70));
    console.log(`  VENDIX LASTTEST – Szenario: ${CURRENT_SCENARIO}`);
    console.log('═'.repeat(70));

    const token = setupAuth();
    return { token };
}

// ═══════════════════════════════════════════════════════════════════════════
// SZENARIEN-DEFINITIONEN
// ═══════════════════════════════════════════════════════════════════════════

const scenarios = {
    // ──────────────────────────────────────────────────────────────────────────
    // 1. LASTTEST – Normalbetrieb
    // ──────────────────────────────────────────────────────────────────────────
    // 3 aktive Kassen, 30 Transaktionen/h insgesamt, 18 Artikel/Bon
    // CASH/CARD variabel, 33% Rabatt (10/30%), 25% Pfand, 1 Storno/min
    // Dauer: 30 min
    // Erwartung: alle Response Times < 2000ms
    lasttest: {
        executor:           'constant-arrival-rate',
        rate:               30,             // 30 Transaktionen
        timeUnit:           '1h',           //   pro Stunde
        duration:           '30m',          // Testdauer
        preAllocatedVUs:    3,              // 3 aktive Kassen
        maxVUs:             5,              // Puffer für Spitzen
        exec:               'lasttest',
    },

    // ──────────────────────────────────────────────────────────────────────────
    // 2. STRESSTEST – Belastungsgrenze finden
    // ──────────────────────────────────────────────────────────────────────────
    // Start: 10 User, +10 alle 60s bis 150 → 14 Minuten Ramp
    // Pro VU: 1 Bon alle 3s mit 3 Artikeln (1 Artikel/s)
    // Danach: 3 Minuten halten auf 150
    stresstest: {
        executor:  'ramping-vus',
        startVUs:  10,
        stages: [
            { duration: '1m', target: 20 },
            { duration: '1m', target: 30 },
            { duration: '1m', target: 40 },
            { duration: '1m', target: 50 },
            { duration: '1m', target: 60 },
            { duration: '1m', target: 70 },
            { duration: '1m', target: 80 },
            { duration: '1m', target: 90 },
            { duration: '1m', target: 100 },
            { duration: '1m', target: 110 },
            { duration: '1m', target: 120 },
            { duration: '1m', target: 130 },
            { duration: '1m', target: 140 },
            { duration: '1m', target: 150 },
            { duration: '3m', target: 150 },   // Haltezeit bei max
            { duration: '1m', target: 0   },   // Ramp-down
        ],
        exec: 'stresstest',
    },

    // ──────────────────────────────────────────────────────────────────────────
    // 3. SPIKE-TEST – Lastspitze simulieren
    // ──────────────────────────────────────────────────────────────────────────
    // Baseline: 3 Kassen mit 2 Trans/min = 6 Trans/min (5 min)
    // Spike: 5 → 20 Kassen in <5min, Transaktionen vervierfacht
    // Haltezeit auf Peak: 20 min (Ziel: 15-30 min)
    // Recovery: zurück auf Baseline in 5 min
    // 70-80% Rabatte aus 46 Rabattstufen (5%-50%)
    spiketest: {
        executor: 'ramping-arrival-rate',
        timeUnit: '1m',
        preAllocatedVUs: 5,
        maxVUs: 30,
        stages: [
            { duration: '5m',  target: 6   },   // Baseline: 6 Trans/min
            { duration: '2m',  target: 24  },   // Vervierfachung
            { duration: '3m',  target: 40  },   // weiter hoch bis Spike-Peak
            { duration: '20m', target: 40  },   // Haltezeit 20 min auf Peak
            { duration: '5m',  target: 6   },   // Recovery zurück auf Baseline
        ],
        startRate: 6,
        exec: 'spiketest',
    },

    // ──────────────────────────────────────────────────────────────────────────
    // 4. SOAK-TEST – Langzeitstabilität über Tagesverlauf
    // ──────────────────────────────────────────────────────────────────────────
    // 06-09 Uhr: 1 Kasse    (3 h)
    // 09-12 Uhr: 3 Kassen   (3 h)
    // 12-14 Uhr: 5 Kassen   (2 h, Mittagsspitze)
    // 14-18 Uhr: 3 Kassen   (4 h)
    // 18-22 Uhr: 4 Kassen   (4 h, Abendspitze)
    // Gesamt: 16 Stunden
    // Ziel: ~2000 Bons total (112.5 Bons/h)
    soaktest: {
        executor: 'ramping-vus',
        startVUs: 1,
        stages: [
            { duration: '3h', target: 1 },   // 06–09 Uhr
            { duration: '3h', target: 3 },   // 09–12 Uhr
            { duration: '2h', target: 5 },   // 12–14 Uhr Mittag
            { duration: '4h', target: 3 },   // 14–18 Uhr
            { duration: '4h', target: 4 },   // 18–22 Uhr Abend
        ],
        gracefulRampDown: '30s',
        exec: 'soaktest',
    },

    // ──────────────────────────────────────────────────────────────────────────
    // 5. CAPACITY-TEST – Multi-dimensionaler Kipppunkt-Test
    // ──────────────────────────────────────────────────────────────────────────
    // VUs (Kassen):  Start 5, +5 alle 2 min → 5/10/15/20/25/30 VUs
    // Artikel/Bon:   wächst mit der Stufe → 50/100/150/200/250/300
    // Jede Stufe: 2 min aktive Last → Gesamtdauer 12 min + 2 min Cooldown
    capacitytest: {
        executor: 'ramping-vus',
        startVUs: 5,
        stages: [
            { duration: '2m', target: 5  },    // Stufe 1: 5 VUs, 50 Art/Bon
            { duration: '2m', target: 10 },    // Stufe 2: 10 VUs, 100 Art/Bon
            { duration: '2m', target: 15 },    // Stufe 3: 15 VUs, 150 Art/Bon
            { duration: '2m', target: 20 },    // Stufe 4: 20 VUs, 200 Art/Bon
            { duration: '2m', target: 25 },    // Stufe 5: 25 VUs, 250 Art/Bon
            { duration: '2m', target: 30 },    // Stufe 6: 30 VUs, 300 Art/Bon
            { duration: '2m', target: 0  },    // Cooldown
        ],
        exec: 'capacitytest',
    },
};

// ═══════════════════════════════════════════════════════════════════════════
// OPTIONS – nur das ausgewählte Szenario aktivieren
// ═══════════════════════════════════════════════════════════════════════════

if (!scenarios[CURRENT_SCENARIO]) {
    throw new Error(
        `Unbekanntes SCENARIO='${CURRENT_SCENARIO}'. ` +
        `Gültig: ${Object.keys(scenarios).join(', ')}`
    );
}

export const options = {
    scenarios: {
        [CURRENT_SCENARIO]: scenarios[CURRENT_SCENARIO],
    },
    // Thresholds: ab wann gilt der Test als "fehlgeschlagen"
    thresholds: {
        'http_req_duration': ['p(95)<2000'],      // P95 < 2 Sekunden
        'http_req_failed':   ['rate<0.05'],        // < 5% Fehlerrate
        'checks':            ['rate>0.95'],        // > 95% Checks erfolgreich
    },
    // Tags für alle Metriken → in Grafana filterbar
    tags: {
        scenario: CURRENT_SCENARIO,
    },
    // Wir wollen die Rohdaten nicht in der Console sehen — nur die Summary am Ende
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(95)', 'p(99)'],
};

// ═══════════════════════════════════════════════════════════════════════════
// EXEC-FUNKTIONEN (eine pro Szenario)
// ═══════════════════════════════════════════════════════════════════════════

// ── 1. Lasttest ──────────────────────────────────────────────────────────────
export function lasttest(data) {
    const cashierId = getCashierId(exec.vu.idInTest);

    runFullBon(data.token, cashierId, {
        articleCount:   18,
        discountChance: 0.33,
        discountRates:  LASTTEST_DISCOUNTS,      // nur 10% oder 30%
        depositChance:  0.25,                     // jeder 4. Bon
        cancelChance:   0.017,                    // ~1 Storno/min bei 30 Trans/h × 3 VUs
    });
}

// ── 2. Stresstest ────────────────────────────────────────────────────────────
export function stresstest(data) {
    const cashierId = getCashierId(exec.vu.idInTest);

    // 1 Bon alle 3s mit 3 Artikeln (1 Artikel/s während Bon-Erstellung)
    const receiptId = createReceipt(
        data.token,
        pickRegisterId(),
        cashierId,
        pickPaymentMethod(),
    );
    if (!receiptId) {
        sleep(3);
        return;
    }

    for (let i = 0; i < 3; i++) {
        addArticleLine(data.token, receiptId, pickArticleId(), 1);
        sleep(1);
    }
}

// ── 3. Spike-Test ────────────────────────────────────────────────────────────
export function spiketest(data) {
    const cashierId = getCashierId(exec.vu.idInTest);

    runFullBon(data.token, cashierId, {
        articleCount:   5,
        discountChance: 0.75,                     // 75% der Bons mit Rabatt
        discountRates:  DISCOUNT_RATES,           // 46 verschiedene Rabattstufen
        depositChance:  0.15,
        cancelChance:   0.02,
    });
}

// ── 4. Soak-Test ─────────────────────────────────────────────────────────────
export function soaktest(data) {
    const cashierId = getCashierId(exec.vu.idInTest);

    runFullBon(data.token, cashierId, {
        articleCount:   15,
        discountChance: 0.25,
        discountRates:  LASTTEST_DISCOUNTS,
        depositChance:  0.20,
        cancelChance:   0.01,
    });

    // Langes Sleep für realistisches Tagesverlauf-Pacing
    // Ziel: ca. 112 Bons/h über alle VUs → ~30s pro Bon pro VU
    sleep(25 + Math.random() * 10);   // 25-35s zwischen Bons
}

// ── 5. Capacity-Test ─────────────────────────────────────────────────────────
export function capacitytest(data) {
    const cashierId = getCashierId(exec.vu.idInTest);

    // Artikelanzahl wächst mit der Stufe — ermittelt anhand Testdauer
    // Stufen: 0-2min=50, 2-4min=100, 4-6min=150, 6-8min=200, 8-10min=250, 10-12min=300
    const elapsedMin  = exec.scenario.iterationInTest > 0
        ? (Date.now() - exec.scenario.startTime) / 60000
        : 0;
    const stage       = Math.min(6, Math.floor(elapsedMin / 2) + 1);
    const articleCount = 50 * stage;

    runFullBon(data.token, cashierId, {
        articleCount:   articleCount,
        discountChance: 0.30,
        discountRates:  LASTTEST_DISCOUNTS,
        depositChance:  0.20,
        cancelChance:   0.05,                      // 5% Stornos für Capacity-Test
    });
}

// ═══════════════════════════════════════════════════════════════════════════
// TEARDOWN – läuft 1x nach allen VUs
// ═══════════════════════════════════════════════════════════════════════════

export function teardown(data) {
    console.log('═'.repeat(70));
    console.log(`  VENDIX LASTTEST BEENDET – Szenario: ${CURRENT_SCENARIO}`);
    console.log('═'.repeat(70));
}