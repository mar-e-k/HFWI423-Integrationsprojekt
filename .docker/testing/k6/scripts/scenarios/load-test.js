import exec from 'k6/execution';
import {executeSharedSetup, executeSharedSummary, baseThresholds} from '../lib/runner-base.js';
import {checkout} from '../lib/client/receipt.js';
import {redeemVoucher} from '../lib/client/voucher.js';
import {pickRegisterId, pickCashierUuid} from '../lib/utils.js';

export const options = {
    scenarios: {
        loadtest: {
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
    tags: {scenario: 'loadtest'},
};

export function setup() {
    return executeSharedSetup('loadtest');
}

export default function (data) {
    const cashierId = pickCashierUuid(exec.vu.idInTest);
    const receiptId = checkout(data.token, pickRegisterId(), cashierId, data.pools.articlePool, {
        articleCount: 5,
        discountChance: 0.0,
        discountRates: [],
        scanGtins: false,
    });

    if (receiptId && Math.random() < 0.20) {
        redeemVoucher(data.token);
    }
}

export function teardown(data) {
    executeSharedSummary(data, 'loadtest');
}
