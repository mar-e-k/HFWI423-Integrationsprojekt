import http from 'k6/http';
import {check} from 'k6';
import {authHeaders} from '../auth.js';
import {ORCHESTRATOR_URL, STORE_ID} from '../config.js';
import {voucherChecksMetric, voucherRedeemsMetric} from '../metrics.js';

export function createVoucher(token, voucherCode, expiresAt = null) {
    const payload = JSON.stringify({
        code: voucherCode,
        expiresAt: expiresAt
    });

    const res = http.post(`${ORCHESTRATOR_URL}/api/voucher`, payload, {
        ...authHeaders(token, STORE_ID),
        tags: {endpoint: 'voucher_create'}
    });

    const success = check(res, {
        'Create voucher returns 200 or 201': (r) => r.status === 200 || r.status === 201,
        'Create response contains UUID code': (r) => r.json('code') === voucherCode,
    });

    return success ? res.json() : null;
}

export function redeemVoucher(token, voucherCode) {
    if (!voucherCode) return null;

    const res = http.post(`${ORCHESTRATOR_URL}/api/voucher/${voucherCode}/redeem`, null, {
        ...authHeaders(token, STORE_ID),
        tags: {endpoint: 'voucher_redeem'},
        responseCallback: http.expectedStatuses(200, 404, 409),
    });

    const isOk = check(res, {
        'Redeem voucher avoids 5xx': (r) => r.status < 500,
        'Redeem voucher successfully updates': (r) => r.status === 200
    });

    if (isOk && res.status === 200) {
        voucherRedeemsMetric.add(1);
        return res.json();
    }
    return null;
}

export function checkAndRedeemVoucher(token, voucherCode) {
    if (!voucherCode) return {checked: false, redeemed: false};

    const checkRes = http.get(`${ORCHESTRATOR_URL}/api/voucher/${voucherCode}`, {
        ...authHeaders(token, STORE_ID),
        tags: {endpoint: 'voucher_check'},
        responseCallback: http.expectedStatuses(200, 404),
    });

    if (checkRes.status !== 200) {
        return {checked: false, redeemed: false};
    }
    voucherChecksMetric.add(1);

    try {
        if (checkRes.json('redeemedAt') !== null) {
            return {checked: true, redeemed: false};
        }
    } catch (_) {
        return {checked: false, redeemed: false};
    }

    const redeemRes = http.post(`${ORCHESTRATOR_URL}/api/voucher/${voucherCode}/redeem`, null, {
        ...authHeaders(token, STORE_ID),
        tags: {endpoint: 'voucher_redeem'},
        responseCallback: http.expectedStatuses(200, 409),
    });

    const success = redeemRes.status === 200;
    if (success) {
        voucherRedeemsMetric.add(1);
    }

    return {checked: true, redeemed: success};
}