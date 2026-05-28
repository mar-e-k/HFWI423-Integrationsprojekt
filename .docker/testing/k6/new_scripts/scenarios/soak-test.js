import exec from 'k6/execution';
import { executeSharedSetup, executeSharedTeardown, baseThresholds } from '../lib/runner-base.js';
import { runFullBon, redeemVoucher } from '../lib/business.js';
import { LASTTEST_DISCOUNTS } from '../lib/config.js';

export const options = {
    scenarios: {
        soaktest_warmup: {
            executor: 'constant-arrival-rate',
            rate: 180,
            timeUnit: '1h',
            duration: '1h',
            preAllocatedVUs: 6,
            maxVUs: 10,
        },
        soaktest: {
            executor: 'ramping-vus',
            startVUs: 6,
            startTime: '1h',
            stages: [
                { duration: '3h', target: 6 },
                { duration: '3h', target: 20 },
                { duration: '2h', target: 30 },
                { duration: '4h', target: 20 },
                { duration: '4h', target: 25 },
            ],
            gracefulRampDown: '30s',
        },
    },
    thresholds: baseThresholds,
    tags: { scenario: 'soaktest' },
};

export function setup() {
    return executeSharedSetup('soaktest');
}

export default function (data) {
    runFullBon(data.token, exec.vu.idInTest, data.pools, {
        articleCount: 15,
        discountChance: 0.25,
        discountRates: LASTTEST_DISCOUNTS,
        depositChance: 0.20,
        cancelChance: 0.01,
        voucherChance: 0.08,
        scanGtins: true,
    });

    if (Math.random() < 0.08) {
        redeemVoucher(data.token);
    }
}

export function teardown(data) {
    executeSharedTeardown('soaktest');
}