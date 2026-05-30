import exec from 'k6/execution';
import {executeSharedSetup, executeSharedSummary, baseThresholds} from '../lib/runner-base.js';
import {checkout} from '../lib/client/receipt.js';
import {redeemVoucher} from '../lib/client/voucher.js';
import {pickRegisterId, pickCashierUuid, pickRandom} from '../lib/utils.js';
import {STRESSTEST_DISCOUNTS} from '../lib/config.js';

export const options = {
    scenarios: {
        capacitytest: {
            executor: 'ramping-vus',
            startVUs: 5,
            stages: [
                {duration: '2m', target: 5},
                {duration: '2m', target: 10},
                {duration: '2m', target: 20},
                {duration: '2m', target: 30},
                {duration: '2m', target: 40},
                {duration: '2m', target: 50},
                {duration: '2m', target: 0},
            ],
        },
    },
    thresholds: baseThresholds,
    tags: {scenario: 'capacitytest'},
};

export function setup() {
    return executeSharedSetup('capacitytest');
}

export default function (data) {
    const elapsedMin = (Date.now() - exec.scenario.startTime) / 60000;
    const stage = Math.min(6, Math.floor(elapsedMin / 2) + 1);
    const articleCount = 100 * stage;

    const receiptId = checkout(data.token, pickRegisterId(), pickCashierUuid(), data.pools.articlePool, {
        articleCount: articleCount,
        discountChance: 0.30,
        discountRates: STRESSTEST_DISCOUNTS,
        scanGtins: false,
    });

    if (receiptId && Math.random() < 0.12 && data.pools.gtinPool?.length) {
        redeemVoucher(data.token, pickRandom(data.pools.gtinPool));
    }
}

export function handleSummary(data) {
    executeSharedSummary(data,'capacitytest');
}
