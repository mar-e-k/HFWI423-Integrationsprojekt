import http from 'k6/http';
import {check} from 'k6';
import {authHeaders} from '../auth.js';
import {ORCHESTRATOR_URL, STORE_ID} from '../config.js';
import {generateUniqueId, pickRandom, pickPaymentMethod, pickGtin} from '../utils.js';
import {scanArticleByGtin} from './article.js';
import {
    bonsCreatedMetric, bonsFailedMetric, checkoutsMetric,
    articlesAddedMetric, discountsMetric, bonPrintsMetric, cancelsMetric, depositsMetric
} from '../metrics.js';

export function getReceipts(token) {
    const res = http.get(`${ORCHESTRATOR_URL}/api/receipt`, {
        ...authHeaders(token, STORE_ID),
        tags: {endpoint: 'get_receipts'}
    });
    return res.status === 200 ? res.json() : [];
}

export function checkout(token, registerId, cashierId, articlePool, opts = {}) {
    const articleCount = opts.articleCount ?? 18;
    const discountChance = opts.discountChance ?? 0.33;
    const discountRates = opts.discountRates ?? [10, 30];
    const scanGtins = opts.scanGtins ?? false;
    const pool = articlePool?.length > 0 ? articlePool : [1, 2, 3, 4, 5];

    const transientReceiptId = generateUniqueId();
    const lines = [];

    for (let i = 0; i < articleCount; i++) {
        let articleId = pool[Math.floor(Math.random() * pool.length)];
        if (scanGtins) {
            const scanned = scanArticleByGtin(token, pickGtin());
            if (scanned) articleId = scanned;
        }

        const applyDiscount = Math.random() < discountChance;

        const discountOverride = applyDiscount
            ? {
                amount: pickRandom(discountRates),
                reason: 'PROMOTIONAL_ADJUSTMENT'
            }
            : null;

        const priceOverride = Math.random() < 0.1
            ? {
                amount: Math.floor(Math.random() * 1000) + 1,
                reason: 'SYSTEM_CORRECTION'
            }
            : null;

        lines.push({
            id: null,
            receiptId: transientReceiptId,
            articleId: Number(articleId),
            articleAmount: 1,
            discountOverride,
            priceOverride
        });
    }

    const payload = JSON.stringify({
        storeId: Number(STORE_ID),
        registerId: Number(registerId),
        cashierUuid: cashierId,
        paymentMethod: opts.paymentMethod ?? pickPaymentMethod(),
        status: 'OPEN',
        lines: lines,
        vouchers: []
    });

    const res = http.post(`${ORCHESTRATOR_URL}/api/receipt/checkout`, payload, {
        ...authHeaders(token, STORE_ID, registerId),
        tags: {endpoint: 'checkout'}
    });

    const ok = check(res, {
        'Checkout status valid': (r) => r.status === 201 || r.status === 200,
        'Response contains receipt id': (r) => r.json('id') != null,
    });

    if (!ok) {
        bonsFailedMetric.add(1);
        return null;
    }

    checkoutsMetric.add(1);
    bonsCreatedMetric.add(1);
    articlesAddedMetric.add(lines.length);
    if (lines.some(l => l.discountOverride !== null)) discountsMetric.add(1);

    return res.json('id');
}

export function printReceipt(token, receiptId) {
    const res = http.put(`${ORCHESTRATOR_URL}/api/receipt/id/${receiptId}/print`, null, {
        ...authHeaders(token),
        tags: {endpoint: 'print_receipt'},
        responseCallback: http.expectedStatuses(200, 409),
    });

    if (check(res, {'Print processed successfully': (r) => r.status === 200 || r.status === 409})) {
        if (res.status === 200) bonPrintsMetric.add(1);
    }
    return res.status;
}

export function cancelReceipt(token, receiptId) {
    const res = http.put(`${ORCHESTRATOR_URL}/api/receipt/id/${receiptId}/cancel`, null, {
        ...authHeaders(token),
        tags: {endpoint: 'cancel_receipt'},
        responseCallback: http.expectedStatuses(200, 409),
    });

    if (res.status === 200) cancelsMetric.add(1);
    return res.status;
}

export function depositReturn(token, registerId, cashierId, depositPool) {
    const pool = depositPool?.length > 0 ? depositPool : [1];
    const transientReceiptId = generateUniqueId(); // Cleaned up random function fallback
    const articleId = pool[Math.floor(Math.random() * pool.length)];

    const payload = JSON.stringify({
        storeId: Number(STORE_ID),
        registerId: Number(registerId),
        cashierUuid: cashierId,
        paymentMethod: pickPaymentMethod(),
        status: 'OPEN',
        lines: [{
            id: null,
            receiptId: transientReceiptId,
            articleId: Number(articleId),
            articleAmount: 1,
            discountOverride: null,
            priceOverride: null
        }],
        vouchers: []
    });

    const res = http.post(`${ORCHESTRATOR_URL}/api/receipt/checkout`, payload, {
        ...authHeaders(token, STORE_ID, registerId),
        tags: {endpoint: 'deposit_return'}
    });

    if (check(res, {'Deposit returns valid status': (r) => r.status === 201 || r.status === 200})) {
        depositsMetric.add(1);
        try {
            const id = res.json('id');
            if (id) printReceipt(token, id);
        } catch (_) {
        }
    }
}
