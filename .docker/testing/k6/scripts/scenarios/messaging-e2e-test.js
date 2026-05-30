import {sleep, check} from 'k6';
import {randomBetween, pickRandom} from '../lib/utils.js';
import {
    createReplenishmentOrder,
    getReplenishmentOrderStatus,
    getStoreStock,
} from '../lib/client/messaging.js';
import {executeSharedSetup, executeSharedSummary} from '../lib/runner-base.js';

const ORDER_RATE = Number(__ENV.ORDER_RATE || 30);
const ORDER_DURATION = __ENV.ORDER_DURATION || '5m';
const URGENT_RATIO = Number(__ENV.URGENT_RATIO || 0.3);
const VERIFY_STOCK = (__ENV.VERIFY_STOCK || 'true') === 'true';
const VERIFY_WAIT_MS = Number(__ENV.VERIFY_WAIT_MS || 2000);
const POLL_STATUS = (__ENV.POLL_STATUS || 'true') === 'true';

const TEST_ARTICLE_IDS = Array.from({length: 15}, (_, i) => i + 1);

export const options = {
    scenarios: {
        messaging_e2e: {
            executor: 'constant-arrival-rate',
            rate: ORDER_RATE,
            timeUnit: '1m',
            duration: ORDER_DURATION,
            preAllocatedVUs: 5,
            maxVUs: 20,
            exec: 'messagingE2E',
        },
    },
    thresholds: {
        'vendix_messaging_stock_check_failed': ['count<5'],
        'vendix_replenishment_ack_ms': ['p(95)<500'],
        'vendix_replenishment_orders_failed': ['count<10'],
        http_req_failed: ['rate<0.02'],
    },
};

export function messagingE2E({token}) {
    const articleId = pickRandom(TEST_ARTICLE_IDS);
    const amount = randomBetween(1, 10);
    const isUrgent = Math.random() < URGENT_RATIO;

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

export function setup() {
    return executeSharedSetup('messaging-e2e-test');
}

export function teardown(data) {
    executeSharedSummary(data, 'messaging-e2e-test');
}
