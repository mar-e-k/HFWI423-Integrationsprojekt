import {executeSharedSetup, executeSharedSummary, baseThresholds} from '../lib/runner-base.js';
import {runFullBon} from '../lib/workflow/cashier-flow.js';
import {createAndRedeemVoucher} from '../lib/client/voucher.js';
import {STRESSTEST_DISCOUNTS} from '../lib/config.js';
import {sleepBetween} from '../lib/utils.js';

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
            executor: 'constant-arrival-rate',
            rate: 180,
            timeUnit: '1h',
            startTime: '1h',
            duration: '16h',
            preAllocatedVUs: 6,
            maxVUs: 30,
        },
    },
    thresholds: baseThresholds,
    tags: {scenario: 'soaktest'},
};

export function setup() {
    return executeSharedSetup('soaktest');
}

export default function (data) {
    runFullBon(data.token, data.pools, {
        articleCount: 15,
        discountChance: 0.25,
        discountRates: STRESSTEST_DISCOUNTS,
        depositChance: 0.20,
        cancelChance: 0.01,
        voucherChance: 0.08,
        scanGtins: true,
        scanPaceMinMs: 600,
        scanPaceMaxMs: 1100,
        stepPaceMinMs: 250,
        stepPaceMaxMs: 750,
    });

    if (Math.random() < 0.08) {
        sleepBetween(250, 750);
        createAndRedeemVoucher(data.token);
    }
}

export function teardown(data) {
    executeSharedSummary(data, 'soaktest');
}
