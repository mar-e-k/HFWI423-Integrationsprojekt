import {executeSharedSetup, executeSharedSummary, baseThresholds} from '../lib/runner-base.js';
import {runFullBon} from '../lib/workflow/cashier-flow.js';
import {redeemVoucher} from '../lib/client/voucher.js';
import {STRESSTEST_DISCOUNTS} from '../lib/config.js';

export const options = {
    scenarios: {
        loadtest: {
            executor: 'constant-arrival-rate',
            rate: 180,
            timeUnit: '1h',
            duration: '30m',
            preAllocatedVUs: 6,
            maxVUs: 10,
        },
    },
    thresholds: {
        ...baseThresholds,
        'http_req_duration{endpoint:checkout}': ['p(95)<2000'],
        'http_req_duration{endpoint:scan_gtin}': ['p(95)<500'],
    },
    tags: {scenario: 'loadtest'},
};

export function setup() {
    return executeSharedSetup('loadtest');
}

export default function (data) {
    runFullBon(data.token, data.pools, {
        articleCount: 20,
        discountChance: 0.33,
        discountRates: STRESSTEST_DISCOUNTS,
        depositChance: 0.25,
        cancelChance: 0.017,
        voucherChance: 0.10,
        scanGtins: true,
    });

    if (Math.random() < 0.10) {
        redeemVoucher(data.token);
    }
}

export function teardown(data) {
    executeSharedSummary(data, 'loadtest');
}
