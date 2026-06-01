import http from 'k6/http';
import {check} from 'k6';
import {authHeaders} from '../auth.js';
import {ORCHESTRATOR_URL, STORE_ID} from '../config.js';
import {voucherChecksMetric, voucherRedeemsMetric} from '../metrics.js';
import {generateUuid, pickRandom} from '../utils.js';

export function getAllVouchers(token) {
    const res = http.get(`${ORCHESTRATOR_URL}/api/voucher`, {
        ...authHeaders(token, STORE_ID),
        tags: {endpoint: 'get_all_vouchers'}
    });

    if (res.status !== 200) {
        console.error(`Failed to fetch vouchers. Status: ${res.status}`);
        return [];
    }
    return res.json();
}

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

export function redeemVoucherCode(token, voucherCode) {
    if (!voucherCode) return null;

    const res = http.put(`${ORCHESTRATOR_URL}/api/voucher/${voucherCode}/redeem`, null, {
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

export function redeemVoucher(token, voucherPool) {
    const voucherCode = pickRandom(voucherPool);
    return redeemVoucherCode(token, voucherCode);
}

export function createAndRedeemVoucher(token) {
    const voucherCode = generateUuid();
    const voucher = createVoucher(token, voucherCode);
    return voucher ? redeemVoucherCode(token, voucherCode) : null;
}

export function checkAndRedeemVoucherCode(token, voucherCode) {
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

    const redeemRes = http.put(`${ORCHESTRATOR_URL}/api/voucher/${voucherCode}/redeem`, null, {
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

export function checkAndRedeemVoucher(token, voucherPool) {
    return checkAndRedeemVoucherCode(token, pickRandom(voucherPool));
}

export function createCheckAndRedeemVoucher(token) {
    const voucherCode = generateUuid();
    const voucher = createVoucher(token, voucherCode);
    return voucher
        ? checkAndRedeemVoucherCode(token, voucherCode)
        : {checked: false, redeemed: false};
}
