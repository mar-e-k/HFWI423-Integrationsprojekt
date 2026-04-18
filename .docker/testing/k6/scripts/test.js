// ═══════════════════════════════════════════════════════════════════════════
// test.js – Vendix Kassensystem Lasttests (5 Szenarien)
//
// Starten:
//   docker exec vendix-k6 k6 run -o experimental-prometheus-rw \
//     /etc/k6/scripts/test.js -e SCENARIO=lasttest
//
// Szenarien:
//   lasttest     – Normalbetrieb:    6 VUs, 180 Trans/h, GTIN-Scans, Voucher
//   stresstest   – Belastungsgrenze: 10→200 VUs, kein Sleep, GTIN-Scans
//   spiketest    – Lastspitze:       Baseline→80 Trans/min→Recovery
//   soaktest     – Memory-Drift:     12–30 VUs, kein Sleep, 16h
//   capacitytest – Kipppunkt:        5→50 VUs, 100→600 Artikel/Bon
// ═══════════════════════════════════════════════════════════════════════════

import exec from 'k6/execution';

import { CURRENT_SCENARIO, LASTTEST_DISCOUNTS, DISCOUNT_RATES } from './lib/config.js';
import { setupAuth } from './lib/auth.js';
import {
    preloadArticlePool,
    runFullBon,
    checkout,
    redeemVoucher,
    pickRegisterId,
    pickCashierId,
} from './lib/business.js';

// ═══════════════════════════════════════════════════════════════════════════
// SETUP — 1× vor allen VUs
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
    // 1. LASTTEST – Normalbetrieb
    // ──────────────────────────────────────────────────────────────────────
    lasttest: {
        executor:        'constant-arrival-rate',
        rate:            180,
        timeUnit:        '1h',
        duration:        '30m',
        preAllocatedVUs: 6,
        maxVUs:          10,
        exec:            'lasttest',
    },

    // ──────────────────────────────────────────────────────────────────────
    // 2. STRESSTEST – Belastungsgrenze (kein Sleep)
    // ──────────────────────────────────────────────────────────────────────
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
            { duration: '3m', target: 200 },
            { duration: '2m', target: 0   },
        ],
        exec: 'stresstest',
    },

    // ──────────────────────────────────────────────────────────────────────
    // 3. SPIKE-TEST – Lastspitze + Recovery
    // ──────────────────────────────────────────────────────────────────────
    spiketest: {
        executor:        'ramping-arrival-rate',
        timeUnit:        '1m',
        preAllocatedVUs: 10,
        maxVUs:          40,
        startRate:       6,
        stages: [
            { duration: '3m',  target: 6  },
            { duration: '1m',  target: 80 },
            { duration: '20m', target: 80 },
            { duration: '5m',  target: 6  },
            { duration: '2m',  target: 0  },
        ],
        exec: 'spiketest',
    },

    // ──────────────────────────────────────────────────────────────────────
    // 4. SOAK-TEST – Memory-Drift (kein Sleep)
    // ──────────────────────────────────────────────────────────────────────
    soaktest: {
        executor:         'ramping-vus',
        startVUs:         12,
        stages: [
            { duration: '3h', target: 12 },
            { duration: '3h', target: 20 },
            { duration: '2h', target: 30 },
            { duration: '4h', target: 20 },
            { duration: '4h', target: 25 },
        ],
        gracefulRampDown: '30s',
        exec: 'soaktest',
    },

    // ──────────────────────────────────────────────────────────────────────
    // 5. CAPACITY-TEST – Kipppunkt (kein Sleep)
    // ──────────────────────────────────────────────────────────────────────
    capacitytest: {
        executor: 'ramping-vus',
        startVUs: 5,
        stages: [
            { duration: '2m', target: 5  },
            { duration: '2m', target: 10 },
            { duration: '2m', target: 20 },
            { duration: '2m', target: 30 },
            { duration: '2m', target: 40 },
            { duration: '2m', target: 50 },
            { duration: '2m', target: 0  },
        ],
        exec: 'capacitytest',
    },
};

// ─── Validierung ─────────────────────────────────────────────────────────────

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
        'http_req_duration':                            ['p(95)<2000'],
        'http_req_failed':                              ['rate<0.05'],
        'checks':                                       ['rate>0.95'],
        'http_req_duration{endpoint:checkout}':         ['p(95)<2000'],
        'http_req_duration{endpoint:scan_gtin}':        ['p(95)<500'],
        'http_req_duration{endpoint:print_receipt}':    ['p(95)<1000'],
        'http_req_duration{endpoint:voucher_redeem}':   ['p(95)<1000'],
    },
    tags:              { scenario: CURRENT_SCENARIO },
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(95)', 'p(99)'],
};

// ═══════════════════════════════════════════════════════════════════════════
// EXEC-FUNKTIONEN
// ═══════════════════════════════════════════════════════════════════════════

// ── 1. Lasttest ──────────────────────────────────────────────────────────────
// runFullBon ruft intern checkAndRedeemVoucher auf (GET + POST).
// Zusätzlich: jeder 10. Bon löst direkt per redeemVoucher ein.
export function lasttest(data) {
    runFullBon(data.token, exec.vu.idInTest, data.pools, {
        articleCount:   20,
        discountChance: 0.33,
        discountRates:  LASTTEST_DISCOUNTS,
        depositChance:  0.25,
        cancelChance:   0.017,
        voucherChance:  0.10,   // checkAndRedeemVoucher (GET+POST)
        scanGtins:      true,
    });

    // Zusätzlich: ~10% der Bons mit direktem Voucher-Scan (nur POST, kein GET)
    if (Math.random() < 0.10) redeemVoucher(data.token);
}

// ── 2. Stresstest ────────────────────────────────────────────────────────────
// Kein runFullBon — maximaler Dauerfeuer für Pool-Erschöpfung.
// Jeder 5. Checkout löst danach direkt einen Voucher ein.
export function stresstest(data) {
    const cashierId = pickCashierId(exec.vu.idInTest);

    const receiptId = checkout(data.token, pickRegisterId(), cashierId, data.pools.articlePool, {
        articleCount:   5,
        discountChance: 0.0,
        discountRates:  [],
        scanGtins:      true,
    });

    // Jeder 5. Bon mit Voucher — erhöht DB-Last durch zusätzliche Transaktionen
    if (receiptId && Math.random() < 0.20) redeemVoucher(data.token);
}

// ── 3. Spike-Test ────────────────────────────────────────────────────────────
// Hohe Voucher-Quote unter Spike-Last (15% im runFullBon + 15% direkt).
export function spiketest(data) {
    runFullBon(data.token, exec.vu.idInTest, data.pools, {
        articleCount:   8,
        discountChance: 0.75,
        discountRates:  DISCOUNT_RATES,
        depositChance:  0.20,
        cancelChance:   0.02,
        voucherChance:  0.15,   // checkAndRedeemVoucher
        scanGtins:      false,
    });

    // Zusätzlich direktes Einlösen unter Spike-Last
    if (Math.random() < 0.15) redeemVoucher(data.token);
}

// ── 4. Soak-Test ─────────────────────────────────────────────────────────────
// Über 16h werden Voucher kontinuierlich eingelöst — testet ob Voucher-Tabelle
// nach tausenden Einlösungen noch performant antwortet.
export function soaktest(data) {
    runFullBon(data.token, exec.vu.idInTest, data.pools, {
        articleCount:   15,
        discountChance: 0.25,
        discountRates:  LASTTEST_DISCOUNTS,
        depositChance:  0.20,
        cancelChance:   0.01,
        voucherChance:  0.08,   // checkAndRedeemVoucher
        scanGtins:      true,
    });

    // Direktes Einlösen in 8% der Iterationen
    if (Math.random() < 0.08) redeemVoucher(data.token);
}

// ── 5. Capacity-Test ─────────────────────────────────────────────────────────
// Artikel/Bon wächst stufenweise — Voucher-Einlösungen testen ob der DB-Write
// unter steigender Checkout-Last noch funktioniert.
export function capacitytest(data) {
    const cashierId    = pickCashierId(exec.vu.idInTest);
    const elapsedMin   = (Date.now() - exec.scenario.startTime) / 60000;
    const stage        = Math.min(6, Math.floor(elapsedMin / 2) + 1);
    const articleCount = 100 * stage;

    const receiptId = checkout(data.token, pickRegisterId(), cashierId, data.pools.articlePool, {
        articleCount:   articleCount,
        discountChance: 0.30,
        discountRates:  LASTTEST_DISCOUNTS,
        scanGtins:      false,
    });

    // Nach jedem Checkout: 12% Chance auf Voucher-Einlösung
    if (receiptId && Math.random() < 0.12) redeemVoucher(data.token);
}

// ═══════════════════════════════════════════════════════════════════════════
// TEARDOWN
// ═══════════════════════════════════════════════════════════════════════════

export function teardown(data) {
    console.log('═'.repeat(70));
    console.log(`  VENDIX LASTTEST BEENDET – Szenario: ${CURRENT_SCENARIO}`);
    console.log('═'.repeat(70));
}