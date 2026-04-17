// ═══════════════════════════════════════════════════════════════════════════
// test.js – Vendix Kassensystem Lasttests (6 Szenarien)
//
// Starten:
//   docker exec vendix-k6 k6 run -o experimental-prometheus-rw \
//     /etc/k6/scripts/test.js -e SCENARIO=lasttest
//
// Szenarien:
//   lasttest       – Normalbetrieb:    3 Kassen, echte GTIN-Scans, Voucher 10%
//   stresstest     – Belastungsgrenze: 10→200 VUs, kein Sleep, GTIN-Scans
//   spiketest      – Lastspitze:       Baseline→80 Trans/min→Recovery
//   soaktest       – Memory-Drift:     12–30 VUs, KEIN Sleep, 16h
//   capacitytest   – Kipppunkt:        5→50 VUs, 100→600 Artikel/Bon
//   concurrencytest– Race Condition:   20 VUs auf 1 Voucher + 1 Register
// ═══════════════════════════════════════════════════════════════════════════

import { sleep } from 'k6';
import exec from 'k6/execution';

import { CURRENT_SCENARIO, LASTTEST_DISCOUNTS, DISCOUNT_RATES, VOUCHER_RACE_CODE } from './lib/config.js';
import { setupAuth, getCashierId } from './lib/auth.js';
import {
    preloadArticlePool, runFullBon, checkout,
    redeemVoucher, pickRegisterId, pickRandom,
    pickCashierId,
} from './lib/business.js';

// ═══════════════════════════════════════════════════════════════════════════
// SETUP — 1× vor allen VUs
// Lädt JWT + Artikel-Pool via GTIN-Scan aus der echten DB
// ═══════════════════════════════════════════════════════════════════════════

export function setup() {
    console.log('═'.repeat(70));
    console.log(`  VENDIX LASTTEST – Szenario: ${CURRENT_SCENARIO}`);
    console.log('═'.repeat(70));

    const token = setupAuth();
    const { articlePool, depositPool } = preloadArticlePool(token);

    console.log(`[Setup] Bereit. Token: OK | Artikel: ${articlePool.length} | Pfand: ${depositPool.length}`);
    return { token, pools: { articlePool, depositPool } };
}

// ═══════════════════════════════════════════════════════════════════════════
// SZENARIEN
// ═══════════════════════════════════════════════════════════════════════════

const scenarios = {

    // ──────────────────────────────────────────────────────────────────────
    // 1. LASTTEST – Normalbetrieb (schärfer: 180/h statt 90/h, GTIN-Scans)
    // ──────────────────────────────────────────────────────────────────────
    // Ziel: Ressourcen unter realistischer Last — HikariCP und Tomcat
    // Erwartet: stabil, P95 < 2000ms, Fehlerrate < 1%
    // Kipppunkt: HikariCP pending > 0 oder P95 > 1500ms
    lasttest: {
        executor:        'constant-arrival-rate',
        rate:            180,           // 180 Trans/h = 3/min = 1 alle 20s auf 3 Kassen
        timeUnit:        '1h',
        duration:        '30m',
        preAllocatedVUs: 6,
        maxVUs:          10,
        exec:            'lasttest',
    },

    // ──────────────────────────────────────────────────────────────────────
    // 2. STRESSTEST – Belastungsgrenze (schärfer: bis 200 VUs, kein Sleep)
    // ──────────────────────────────────────────────────────────────────────
    // Ziel: HikariCP-Pool und Tomcat-Thread-Pool ausreizen
    // Erwartet: ab ~80-120 VUs Fehler und Timeouts
    // Kipppunkt: hikaricp_connections_pending > 0 dauerhaft
    stresstest: {
        executor: 'ramping-vus',
        startVUs: 10,
        stages: [
            { duration: '1m', target: 20  },
            { duration: '1m', target: 40  },
            { duration: '1m', target: 60  },
            { duration: '1m', target: 80  },
            { duration: '1m', target: 100 },
            { duration: '1m', target: 120 },
            { duration: '1m', target: 140 },
            { duration: '1m', target: 160 },
            { duration: '1m', target: 180 },
            { duration: '1m', target: 200 },
            { duration: '3m', target: 200 },   // Haltezeit auf Maximum
            { duration: '2m', target: 0   },   // Ramp-down
        ],
        exec: 'stresstest',
    },

    // ──────────────────────────────────────────────────────────────────────
    // 3. SPIKE-TEST – Lastspitze (schärfer: Peak 80/min statt 40/min)
    // ──────────────────────────────────────────────────────────────────────
    // Ziel: Systemverhalten bei abruptem Lastanstieg testen + Recovery
    // Erwartet: Spike verursacht 5xx oder Timeouts, Recovery messung kritisch
    spiketest: {
        executor:        'ramping-arrival-rate',
        timeUnit:        '1m',
        preAllocatedVUs: 10,
        maxVUs:          40,
        startRate:       6,
        stages: [
            { duration: '3m',  target: 6  },   // Baseline
            { duration: '1m',  target: 80 },   // Spike abrupt
            { duration: '20m', target: 80 },   // Hochlast halten
            { duration: '5m',  target: 6  },   // Recovery
            { duration: '2m',  target: 0  },   // Cooldown
        ],
        exec: 'spiketest',
    },

    // ──────────────────────────────────────────────────────────────────────
    // 4. SOAK-TEST – Memory-Drift (schärfer: kein Sleep, 12–30 VUs)
    // ──────────────────────────────────────────────────────────────────────
    // Ziel: Memory-Leak und Heap-Drift über 16h aufdecken
    // Erwartet: JVM-Heap wächst schleichend, GC-Pausen nehmen zu
    // Kipppunkt: Heap > 80%, GC-Pause-Zeit verdoppelt sich
    soaktest: {
        executor:         'ramping-vus',
        startVUs:         12,
        stages: [
            { duration: '3h', target: 12 },   // 06–09 Uhr: 12 VUs
            { duration: '3h', target: 20 },   // 09–12 Uhr: 20 VUs
            { duration: '2h', target: 30 },   // 12–14 Uhr: Mittag 30 VUs
            { duration: '4h', target: 20 },   // 14–18 Uhr: 20 VUs
            { duration: '4h', target: 25 },   // 18–22 Uhr: Abend 25 VUs
        ],
        gracefulRampDown: '30s',
        exec: 'soaktest',
        // KEIN Sleep in der exec-Funktion! VUs arbeiten durchgehend.
    },

    // ──────────────────────────────────────────────────────────────────────
    // 5. CAPACITY-TEST – Multi-dimensionaler Kipppunkt (schärfer: bis 50 VUs)
    // ──────────────────────────────────────────────────────────────────────
    // VUs: 5→10→20→30→40→50 (+5 Stufen)
    // Artikel/Bon: 100→200→300→400→500→600 (parallel zu VUs)
    // Ziel: Kipppunkt bei kombinierter Last aus VUs UND Payload-Größe
    capacitytest: {
        executor: 'ramping-vus',
        startVUs: 5,
        stages: [
            { duration: '2m', target: 5  },   // Stufe 1: 5 VUs, 100 Art/Bon
            { duration: '2m', target: 10 },   // Stufe 2: 10 VUs, 200 Art/Bon
            { duration: '2m', target: 20 },   // Stufe 3: 20 VUs, 300 Art/Bon
            { duration: '2m', target: 30 },   // Stufe 4: 30 VUs, 400 Art/Bon
            { duration: '2m', target: 40 },   // Stufe 5: 40 VUs, 500 Art/Bon
            { duration: '2m', target: 50 },   // Stufe 6: 50 VUs, 600 Art/Bon
            { duration: '2m', target: 0  },   // Cooldown
        ],
        exec: 'capacitytest',
    },

    // ──────────────────────────────────────────────────────────────────────
    // 6. CONCURRENCY-TEST – Race Conditions
    // ──────────────────────────────────────────────────────────────────────
    // Ziel 1: Voucher-Race — 20 VUs lösen DENSELBEN Voucher gleichzeitig ein.
    //         Erwartung: genau 1× Status 200, alle anderen 409.
    //         Bug-Signal: mehr als 1× 200 oder irgendein 500.
    //
    // Ziel 2: Register-Race — 30 VUs schreiben gleichzeitig auf Kasse 1.
    //         Erwartung: alle 201, keine Daten-Anomalien, keine 500.
    //
    // Laufzeit: 2 Minuten reichen für klare Race-Condition-Messung.
    concurrencytest: {
        executor:    'shared-iterations',
        vus:         20,
        iterations:  100,
        maxDuration: '2m',
        exec:        'concurrencytest',
    },
};

// ─── Szenario-Validierung ─────────────────────────────────────────────────────

if (!scenarios[CURRENT_SCENARIO]) {
    throw new Error(
        `Unbekanntes SCENARIO='${CURRENT_SCENARIO}'. ` +
        `Gültig: ${Object.keys(scenarios).join(', ')}`
    );
}

// ─── Options ─────────────────────────────────────────────────────────────────

export const options = {
    scenarios: {
        [CURRENT_SCENARIO]: scenarios[CURRENT_SCENARIO],
    },
    thresholds: {
        // Global
        'http_req_duration':                            ['p(95)<2000'],
        'http_req_failed':                              ['rate<0.05'],
        'checks':                                       ['rate>0.95'],
        // Endpunkt-spezifisch
        'http_req_duration{endpoint:checkout}':         ['p(95)<2000'],
        'http_req_duration{endpoint:scan_gtin}':        ['p(95)<500'],
        'http_req_duration{endpoint:voucher_check}':    ['p(95)<500'],
        'http_req_duration{endpoint:voucher_redeem}':   ['p(95)<1000'],
        // Concurrency: kein 500 erlaubt
        'checks{endpoint:voucher_race_redeem}':         ['rate==1.0'],
    },
    tags:             { scenario: CURRENT_SCENARIO },
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(95)', 'p(99)'],
};

// ═══════════════════════════════════════════════════════════════════════════
// EXEC-FUNKTIONEN
// ═══════════════════════════════════════════════════════════════════════════

// ── 1. Lasttest ──────────────────────────────────────────────────────────────
// Echter Kassierer-Flow: GTIN-Scan pro Artikel, Voucher 10% der Bons
export function lasttest(data) {
    runFullBon(data.token, exec.vu.idInTest, data.pools, {
        articleCount:   20,
        discountChance: 0.33,
        discountRates:  LASTTEST_DISCOUNTS,
        depositChance:  0.25,
        cancelChance:   0.017,
        voucherChance:  0.10,        // 10% der Bons mit Voucher
        scanGtins:      true,        // echter GTIN-Scan pro Artikel
    });
}

// ── 2. Stresstest ────────────────────────────────────────────────────────────
// Kein Sleep: VUs arbeiten so schnell wie möglich — maximale Datenbankbelastung
export function stresstest(data) {
    const cashierId = pickCashierId(exec.vu.idInTest);

    // GTIN-Scan + Checkout mit 5 Artikeln — kein Sleep → Dauerfeuer
    checkout(data.token, pickRegisterId(), cashierId, data.pools.articlePool, {
        articleCount:   5,
        discountChance: 0.0,
        discountRates:  [],
        scanGtins:      true,   // GTIN-Scans erhöhen zusätzlich die DB-Last
    });
    // Kein sleep() → nächste Iteration sofort
}

// ── 3. Spike-Test ────────────────────────────────────────────────────────────
// Hohe Rabattquote + Voucher-Einlösungen unter Spike-Last
export function spiketest(data) {
    runFullBon(data.token, exec.vu.idInTest, data.pools, {
        articleCount:   8,
        discountChance: 0.75,
        discountRates:  DISCOUNT_RATES,
        depositChance:  0.20,
        cancelChance:   0.02,
        voucherChance:  0.15,
        scanGtins:      false,   // Kein GTIN-Scan um Spike-Ursache isolierbar zu halten
    });
}

// ── 4. Soak-Test ─────────────────────────────────────────────────────────────
// KEIN Sleep — VUs laufen durchgehend für maximalen Memory-Drift
// Mit GTIN-Scans für realistischeres Request-Pattern
export function soaktest(data) {
    runFullBon(data.token, exec.vu.idInTest, data.pools, {
        articleCount:   15,
        discountChance: 0.25,
        discountRates:  LASTTEST_DISCOUNTS,
        depositChance:  0.20,
        cancelChance:   0.01,
        voucherChance:  0.08,
        scanGtins:      true,   // GTIN-Scans erzeugen mehr SELECT-Queries → mehr GC-Druck
    });
    // Kein sleep() → nächste Iteration sofort → Memory-Drift wird sichtbar
}

// ── 5. Capacity-Test ─────────────────────────────────────────────────────────
// Artikel-Anzahl wächst mit jeder Stufe (100/200/300/400/500/600)
export function capacitytest(data) {
    const cashierId = pickCashierId(exec.vu.idInTest);

    const elapsedMin   = (Date.now() - exec.scenario.startTime) / 60000;
    const stage        = Math.min(6, Math.floor(elapsedMin / 2) + 1);
    const articleCount = 100 * stage;  // 100 / 200 / 300 / 400 / 500 / 600

    checkout(data.token, pickRegisterId(), cashierId, data.pools.articlePool, {
        articleCount:   articleCount,
        discountChance: 0.30,
        discountRates:  LASTTEST_DISCOUNTS,
        scanGtins:      false,  // Kein GTIN-Scan — Kipppunkt soll Payload-Größe sein
    });
    // Kein Sleep — maximale Last pro VU
}

// ── 6. Concurrency-Test ──────────────────────────────────────────────────────
// Phase A (erste 50 Iterationen): Voucher-Race-Condition
//   → Alle 20 VUs lösen DENSELBEN Voucher ein
//   → Erwartung: exakt 1× 200, alle anderen 409
//   → Bug: mehr als 1× 200 = Transaktion fehlt / kein @Version
//
// Phase B (Iterationen 51–100): Register-Race
//   → 20 VUs schreiben gleichzeitig auf Register 1
//   → Erwartung: alle 201, keine 500
export function concurrencytest(data) {
    const iteration = exec.scenario.iterationInTest;

    if (iteration < 50) {
        // Phase A: Voucher-Race
        const status = redeemVoucher(data.token, VOUCHER_RACE_CODE);

        // Logging: Wie viele 200er kommen durch?
        if (status === 200) {
            console.log(`[Race] VU ${exec.vu.idInTest}: Voucher eingelöst (200) – Iteration ${iteration}`);
        }
    } else {
        // Phase B: Register-Race — alle auf Kasse 1
        checkout(data.token, 1, pickCashierId(exec.vu.idInTest), data.pools.articlePool, {
            articleCount:   3,
            discountChance: 0.0,
            discountRates:  [],
            paymentMethod:  'CASH',
        });
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// TEARDOWN
// ═══════════════════════════════════════════════════════════════════════════

export function teardown(data) {
    console.log('═'.repeat(70));
    console.log(`  VENDIX LASTTEST BEENDET – Szenario: ${CURRENT_SCENARIO}`);
    console.log('═'.repeat(70));
}