import http from 'k6/http';
import { check } from 'k6';
import { authHeaders } from '../auth.js';
import { ORCHESTRATOR_URL, VOUCHER_REGULAR_CODES } from '../config.js';
import { pickRandom } from '../utils.js';
import { voucherChecksMetric, voucherRedeemsMetric } from '../metrics.js';

export function redeemVoucher(token) {
    if (!VOUCHER_REGULAR_CODES?.length) return;
    const code = pickRandom(VOUCHER_REGULAR_CODES);

    const res = http.post(`${ORCHESTRATOR_URL}/api/voucher/${code}/redeem`, null, {
        ...authHeaders(token),
        tags: { endpoint: 'voucher_redeem' },
        responseCallback: http.expectedStatuses(200, 404, 409),
    });

    if (check(res, { 'Redeem code avoids 5xx': (r) => r.status !== 500 }) && res.status === 200) {
        voucherRedeemsMetric.add(1);
    }
}

export function checkAndRedeemVoucher(token, voucherCode) {
    const checkRes = http.get(`${ORCHESTRATOR_URL}/api/voucher/${voucherCode}`, {
        ...authHeaders(token), tags: { endpoint: 'voucher_check' },
    });

    if (checkRes.status !== 200) return { checked: false, redeemed: false };
    voucherChecksMetric.add(1);

    try {
        if (checkRes.json('redeemedAt') != null) return { checked: true, redeemed: false };
    } catch (_) {}

    const redeemRes = http.post(`${ORCHESTRATOR_URL}/api/voucher/${voucherCode}/redeem`, null, {
        ...authHeaders(token),
        tags: { endpoint: 'voucher_redeem' },
        responseCallback: http.expectedStatuses(200, 409),
    });

    const success = redeemRes.status === 200;
    if (success) voucherRedeemsMetric.add(1);

    return { checked: true, redeemed: success };
}