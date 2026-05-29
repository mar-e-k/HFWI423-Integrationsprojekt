import exec from 'k6/execution';
import { executeSharedSetup, executeSharedTeardown, baseThresholds } from '../lib/runner-base.js';
import { checkout } from '../lib/client/receipt.js';
import { redeemVoucher } from '../lib/client/voucher.js';
import { pickRegisterId, pickCashierId } from '../lib/utils.js';
import { LASTTEST_DISCOUNTS } from '../lib/config.js';

export const options = {
    scenarios: {
        capacitytest: {
            executor: 'ramping-vus',
            startVUs: 5,
            stages: [
                { duration: '2m', target: 5 },
                { duration: '2m', target: 10 },
                { duration: '2m', target: 20 },
                { duration: '2m', target: 30 },
                { duration: '2m', target: 40 },
                { duration: '2m', target: 50 },
                { duration: '2m', target: 0 },
            ],
        },
    },
    thresholds: baseThresholds,
    tags: { scenario: 'capacitytest' },
};

export function setup() {
    return executeSharedSetup('capacitytest');
}

export default function (data) {
    const cashierId = pickCashierId(exec.vu.idInTest);

    // Calculates elapsed minutes inside the context of this specific scenario engine block
    const elapsedMin = (Date.now() - exec.scenario.startTime) / 60000;
    const stage = Math.min(6, Math.floor(elapsedMin / 2) + 1);
    const articleCount = 100 * stage;

    const receiptId = checkout(data.token, pickRegisterId(), cashierId, data.pools.articlePool, {
        articleCount: articleCount,
        discountChance: 0.30,
        discountRates: LASTTEST_DISCOUNTS,
        scanGtins: false,
    });

    if (receiptId && Math.random() < 0.12) {
        redeemVoucher(data.token);
    }
}

export function teardown(data) {
    executeSharedTeardown('capacitytest');
}
