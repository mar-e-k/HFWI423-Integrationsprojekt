import http from 'k6/http';
import { check } from 'k6';
import { authHeaders } from '../auth.js';
import { ORCHESTRATOR_URL, STORE_ID } from '../config.js';
import { pickRandom, pickPaymentMethod, pickGtin } from '../utils.js';
import { scanArticleByGtin } from './article.js';
import {
    bonsCreatedMetric, bonsFailedMetric, checkoutsMetric,
    articlesAddedMetric, discountsMetric, bonPrintsMetric, cancelsMetric, depositsMetric
} from '../metrics.js';

export function checkout(token, registerId, cashierId, articlePool, opts = {}) {
    const articleCount = opts.articleCount ?? 18;
    const discountChance = opts.discountChance ?? 0.33;
    const discountRates = opts.discountRates ?? [10, 30];
    const scanGtins = opts.scanGtins ?? false;
    const pool = articlePool?.length > 0 ? articlePool : [1, 2, 3, 4, 5];

    const lines = [];
    for (let i = 0; i < articleCount; i++) {
        let articleId = pickRandom(pool);
        if (scanGtins) {
            const scanned = scanArticleByGtin(token, pickGtin());
            if (scanned) articleId = scanned;
        }
        lines.push({
            articleId,
            articleAmount: 1,
            discountPercent: Math.random() < discountChance ? pickRandom(discountRates) : null,
        });
    }

    // Requests are bound securely to the Orchestrator Proxy using context routing keys
    const res = http.post(`${ORCHESTRATOR_URL}/api/receipt/checkout`, JSON.stringify({
        storeId: STORE_ID, registerId, cashierId, paymentMethod: opts.paymentMethod ?? pickPaymentMethod(), lines, returnLineIds: false,
    }), { ...authHeaders(token, STORE_ID, registerId), tags: { endpoint: 'checkout' } });

    const ok = check(res, {
        'Checkout returns 201': (r) => r.status === 201,
        'Checkout body contains receiptId': (r) => r.json('receiptId') != null,
    });

    if (!ok) { bonsFailedMetric.add(1); return null; }

    checkoutsMetric.add(1);
    bonsCreatedMetric.add(1);
    articlesAddedMetric.add(lines.length);
    if (lines.some(l => l.discountPercent != null)) discountsMetric.add(1);

    return res.json('receiptId');
}

export function printReceipt(token, receiptId) {
    const res = http.post(`${ORCHESTRATOR_URL}/api/receipt/${receiptId}/print`, null, {
        ...authHeaders(token),
        tags: { endpoint: 'print_receipt' },
        responseCallback: http.expectedStatuses(200, 409),
    });

    if (check(res, { 'Print returns 200 or 409': (r) => r.status === 200 || r.status === 409 })) {
        if (res.status === 200) bonPrintsMetric.add(1);
    }
    return res.status;
}

export function cancelReceipt(token, receiptId) {
    const res = http.post(`${ORCHESTRATOR_URL}/api/receipt/${receiptId}/cancel`, null, {
        ...authHeaders(token),
        tags: { endpoint: 'cancel_receipt' },
        responseCallback: http.expectedStatuses(200, 409),
    });

    if (res.status === 200) cancelsMetric.add(1);
    return res.status;
}

export function depositReturn(token, registerId, cashierId, depositPool) {
    const pool = depositPool?.length > 0 ? depositPool : [1];
    const res = http.post(`${ORCHESTRATOR_URL}/api/receipt/checkout`, JSON.stringify({
        storeId: STORE_ID, registerId, cashierId,
        paymentMethod: pickPaymentMethod(),
        returnLineIds: false,
        lines: [{ articleId: pickRandom(pool), articleAmount: 1, discountPercent: null }],
    }), { ...authHeaders(token, STORE_ID, registerId), tags: { endpoint: 'deposit_return' } });

    if (check(res, { 'Deposit returns 201': (r) => r.status === 201 })) {
        depositsMetric.add(1);
        try {
            const id = res.json('receiptId');
            if (id) printReceipt(token, id);
        } catch (_) {}
    }
}