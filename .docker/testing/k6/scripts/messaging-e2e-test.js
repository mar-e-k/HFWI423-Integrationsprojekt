// ═══════════════════════════════════════════════════════════════════════════
// messaging-e2e-test.js – Vendix Messaging E2E-Lasttest
//
// Starten (aus Docker):
//   docker exec vendix-k6 k6 run -o experimental-prometheus-rw \
//     /etc/k6/scripts/messaging-e2e-test.js \
//     -e STORE_ID=1 \
//     -e K6_MASTER_TOKEN=<token>
//
// Umgebungsvariablen:
//   STORE_URL          – Store-Basis-URL (default: http://host.docker.internal:8081)
//   STORE_ID           – Store-ID        (default: 1)
//   ORDER_RATE         – Orders/Minute   (default: 30)
//   ORDER_DURATION     – Testdauer       (default: 5m)
//   URGENT_RATIO       – Anteil dringend (default: 0.3 → 30 % urgent)
//   VERIFY_STOCK       – Bestand prüfen  (default: true)
//   VERIFY_WAIT_MS     – Wartezeit vor Verifikation in ms (default: 2000)
//   POLL_STATUS        – Async-Status abfragen (default: true)
// ═══════════════════════════════════════════════════════════════════════════

import { sleep, check } from 'k6';
import { setupAuth } from './lib/auth.js';
import { STORE_ID, NORMAL_ARTICLE_IDS, STORE_URL } from './lib/config.js';
import { assertStoreReachable } from './lib/business.js';
import {
    createReplenishmentOrder,
    getReplenishmentOrderStatus,
    getStoreStock,
} from './lib/messaging.js';

// ── Konfiguration ────────────────────────────────────────────────────────────

const ORDER_RATE     = Number(__ENV.ORDER_RATE     || 30);
const ORDER_DURATION = __ENV.ORDER_DURATION         || '5m';
const URGENT_RATIO   = Number(__ENV.URGENT_RATIO   || 0.3);
const VERIFY_STOCK   = (__ENV.VERIFY_STOCK         || 'true') === 'true';
const VERIFY_WAIT_MS = Number(__ENV.VERIFY_WAIT_MS || 2000);
const POLL_STATUS    = (__ENV.POLL_STATUS          || 'true') === 'true';

// ── Schwellwerte ─────────────────────────────────────────────────────────────

export const options = {
    scenarios: {
        messaging_e2e: {
            executor:        'constant-arrival-rate',
            rate:            ORDER_RATE,
            timeUnit:        '1m',
            duration:        ORDER_DURATION,
            preAllocatedVUs: 5,
            maxVUs:          20,
            exec:            'messagingE2E',
        },
    },

    thresholds: {
        'vendix_messaging_stock_check_failed': ['count<5'],
        'vendix_replenishment_ack_ms':          ['p(95)<500'],
        'vendix_replenishment_orders_failed':   ['count<10'],
        http_req_failed:                       ['rate<0.02'],
    },
};

// ── Setup ────────────────────────────────────────────────────────────────────

export function setup() {
    console.log('═'.repeat(70));
    console.log('  VENDIX MESSAGING E2E-TEST');
    console.log(`  Store-URL   : ${STORE_URL}`);
    console.log(`  Store-ID    : ${STORE_ID}`);
    console.log(`  Rate        : ${ORDER_RATE} Orders/min`);
    console.log(`  Dauer       : ${ORDER_DURATION}`);
    console.log(`  Urgent-Anteil: ${Math.round(URGENT_RATIO * 100)} %`);
    console.log(`  Verifikation: ${VERIFY_STOCK ? 'ja' : 'nein'} (Wartezeit ${VERIFY_WAIT_MS} ms)`);
    console.log('═'.repeat(70));

    const token = setupAuth();
    assertStoreReachable(token);

    const stock = getStoreStock(token, NORMAL_ARTICLE_IDS[0]);
    if (stock === null) {
        console.warn(
            '[Setup] WARNUNG: /api/test/store-stock antwortet nicht für ' +
            `articleId=${NORMAL_ARTICLE_IDS[0]}. ` +
            'Prüfe, ob der Store läuft und der storeContext initialisiert ist.'
        );
    } else {
        console.log(
            `[Setup] Vorab-Stock für articleId=${NORMAL_ARTICLE_IDS[0]}: ` +
            `currentAmount=${stock.currentAmount} (min=${stock.minAmount}, max=${stock.maxAmount})`
        );
    }

    return { token };
}

// ── Haupt-Szenario ───────────────────────────────────────────────────────────

export function messagingE2E({ token }) {
    const articleId = pickRandomArticle();
    const amount    = randomBetween(1, 10);
    const isUrgent  = Math.random() < URGENT_RATIO;

    let stockBefore = null;
    if (VERIFY_STOCK) {
        stockBefore = getStoreStock(token, articleId);
    }

    const order = createReplenishmentOrder(token, articleId, amount, isUrgent);
    if (!order?.correlationId) return;

    if (VERIFY_STOCK && stockBefore !== null) {
        sleep(VERIFY_WAIT_MS / 1000);

        if (POLL_STATUS) {
            const status = getReplenishmentOrderStatus(token, order.correlationId);
            check(status, {
                '[Verify] Async-Status nicht FAILED': (s) => s === null || s.status !== 'FAILED',
            });
        }

        const stockAfter = getStoreStock(token, articleId);
        if (stockAfter !== null) {
            check(stockAfter, {
                '[Verify] Bestand gestiegen oder unverändert': (s) =>
                    s.currentAmount >= stockBefore.currentAmount,
            });

            if (stockAfter.currentAmount > stockBefore.currentAmount) {
                console.debug(
                    `[Verify] ✓ articleId=${articleId}: ` +
                    `${stockBefore.currentAmount} → ${stockAfter.currentAmount} ` +
                    `(+${stockAfter.currentAmount - stockBefore.currentAmount})`
                );
            }
        }
    }
}

// ── Hilfsfunktionen ──────────────────────────────────────────────────────────

function pickRandomArticle() {
    return NORMAL_ARTICLE_IDS[Math.floor(Math.random() * NORMAL_ARTICLE_IDS.length)];
}

function randomBetween(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}
