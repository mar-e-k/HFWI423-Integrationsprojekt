import { sleep, check } from 'k6';
import { setupAuth } from '../lib/auth.js';
import { STORE_ID, ORCHESTRATOR_URL } from '../lib/config.js';
import { assertStoreReachable } from '../lib/client/article.js';
import { randomBetween, pickRandom } from '../lib/utils.js';
import {
    createReplenishmentOrder,
    getReplenishmentOrderStatus,
    getStoreStock,
} from '../lib/client/messaging.js';

const ORDER_RATE     = Number(__ENV.ORDER_RATE     || 30);
const ORDER_DURATION = __ENV.ORDER_DURATION         || '5m';
const URGENT_RATIO   = Number(__ENV.URGENT_RATIO   || 0.3);
const VERIFY_STOCK   = (__ENV.VERIFY_STOCK         || 'true') === 'true';
const VERIFY_WAIT_MS = Number(__ENV.VERIFY_WAIT_MS || 2000);
const POLL_STATUS    = (__ENV.POLL_STATUS          || 'true') === 'true';

const TEST_ARTICLE_IDS = Array.from({ length: 15 }, (_, i) => i + 1);

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

export function setup() {
    console.log('═'.repeat(70));
    console.log('  VENDIX MESSAGING E2E-TEST (GATEWAY ROUTED)');
    console.log(`  Gateway URL : ${ORCHESTRATOR_URL}`);
    console.log(`  Store Target: ID ${STORE_ID}`);
    console.log(`  Load Rate   : ${ORDER_RATE} Orders/min`);
    console.log(`  Duration    : ${ORDER_DURATION}`);
    console.log(`  Urgent Ratio: ${Math.round(URGENT_RATIO * 100)}%`);
    console.log(`  Verify Stock: ${VERIFY_STOCK ? 'Enabled' : 'Disabled'} (${VERIFY_WAIT_MS}ms delay)`);
    console.log('═'.repeat(70));

    const token = setupAuth();
    assertStoreReachable(token);

    const stock = getStoreStock(token, TEST_ARTICLE_IDS[0]);
    if (stock === null) {
        console.warn(`[Setup] WARNING: Store inventory context is unresponsive for articleId=${TEST_ARTICLE_IDS[0]}.`);
    } else {
        console.log(`[Setup] Initial Stock Level for articleId=${TEST_ARTICLE_IDS[0]}: currentAmount=${stock.currentAmount}`);
    }

    return { token };
}

export function messagingE2E({ token }) {
    const articleId = pickRandom(TEST_ARTICLE_IDS);
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
                '[Verify] Async status does not match FAILED': (s) => s === null || s.status !== 'FAILED',
            });
        }

        const stockAfter = getStoreStock(token, articleId);
        if (stockAfter !== null) {
            check(stockAfter, {
                '[Verify] Inventory count increased or stayed stable': (s) => s.currentAmount >= stockBefore.currentAmount,
            });

            if (stockAfter.currentAmount > stockBefore.currentAmount) {
                console.debug(`[Verify] ✓ Article ID ${articleId}: ${stockBefore.currentAmount} → ${stockAfter.currentAmount} (+${stockAfter.currentAmount - stockBefore.currentAmount})`);
            }
        }
    }
}
