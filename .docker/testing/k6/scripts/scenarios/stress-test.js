import {executeSharedSetup, executeSharedSummary, baseThresholds} from '../lib/runner-base.js';
import {checkout} from '../lib/client/receipt.js';
import {createAndRedeemVoucher} from '../lib/client/voucher.js';
import {pickRegisterId, pickCashierUuid} from '../lib/utils.js';

export const options = {
    scenarios: {
        stresstest: {
            executor: 'ramping-vus',
            startVUs: 10,
            stages: [
                {duration: '1m', target: 20},
                {duration: '1m', target: 40},
                {duration: '1m', target: 60},
                {duration: '1m', target: 80},
                {duration: '1m', target: 100},
                {duration: '1m', target: 120},
                {duration: '1m', target: 140},
                {duration: '1m', target: 160},
                {duration: '1m', target: 180},
                {duration: '1m', target: 200},
                {duration: '3m', target: 200},
                {duration: '2m', target: 0},
            ],
        },
    },
    thresholds: baseThresholds,
    tags: {scenario: 'stresstest'},
};

export function setup() {
    return executeSharedSetup('stresstest');
}

export default function (data) {
    const receiptId = checkout(data.token, pickRegisterId(), pickCashierUuid(), data.pools.articlePool, {
        articleCount: 5,
        discountChance: 0.0,
        discountRates: [],
        scanGtins: false,
    });

    if (receiptId && Math.random() < 0.20) {
        createAndRedeemVoucher(data.token);
    }
}

export function teardown(data) {
    executeSharedSummary(data, 'stresstest');
}
