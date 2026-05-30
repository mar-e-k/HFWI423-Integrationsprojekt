import exec from 'k6/execution';
import {executeSharedSetup, executeSharedSummary, baseThresholds} from '../lib/runner-base.js';
import {runFullBon} from '../lib/workflow/cashier-flow.js';
import {redeemVoucher} from '../lib/client/voucher.js';
import {DISCOUNT_RATES} from '../lib/config.js';

export const options = {
    scenarios: {
        spiketest: {
            executor: 'ramping-arrival-rate',
            timeUnit: '1m',
            preAllocatedVUs: 10,
            maxVUs: 40,
            startRate: 6,
            stages: [
                {duration: '3m', target: 6},
                {duration: '1m', target: 80},
                {duration: '20m', target: 80},
                {duration: '5m', target: 6},
                {duration: '2m', target: 0},
            ],
        },
    },
    thresholds: {
        ...baseThresholds,
        'http_req_duration{endpoint:voucher_redeem}': ['p(95)<1000'],
    },
    tags: {scenario: 'spiketest'},
};

export function setup() {
    return executeSharedSetup('spiketest');
}

export default function (data) {
    runFullBon(data.token, exec.vu.idInTest, data.pools, {
        articleCount: 8,
        discountChance: 0.75,
        discountRates: DISCOUNT_RATES,
        depositChance: 0.20,
        cancelChance: 0.02,
        voucherChance: 0.15,
        scanGtins: false,
    });

    if (Math.random() < 0.15) {
        redeemVoucher(data.token);
    }
}

export function teardown(data) {
    executeSharedSummary(data, 'spiketest');
}
