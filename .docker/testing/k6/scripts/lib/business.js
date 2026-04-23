// ═══════════════════════════════════════════════════════════════════════════
// lib/business.js – Vollständiger Kassierer-Workflow
//
// Fachlicher Ablauf (wie ein echter Kassierer):
//   1. Artikel scannen via GTIN   → GET  /api/article/gtin/{gtin}
//   2. Bon abschließen (Checkout) → POST /api/receipt/checkout
//   3. Bon drucken                → POST /api/receipt/{id}/print
//   4. Optional: Voucher einlösen → POST /api/voucher/{code}/redeem
//   5. Optional: Pfand            → POST /api/receipt/checkout (Pfand-Artikel)
//   6. Optional: Stornierung      → POST /api/receipt/{id}/cancel
// ═══════════════════════════════════════════════════════════════════════════

import http   from 'k6/http';
import { check } from 'k6';

import {
    STORE_URL, STORE_ID, REGISTER_IDS, CASHIER_IDS,
    ARTICLE_GTINS, DEPOSIT_ARTICLE_IDS,
    PAYMENT_METHODS, VOUCHER_REGULAR_CODES,
} from './config.js';
import { authHeaders } from './auth.js';
import {
    bonsCreatedMetric, bonsFailedMetric, checkoutsMetric,
    articlesAddedMetric, cancelsMetric, depositsMetric,
    discountsMetric, voucherChecksMetric, voucherRedeemsMetric,
    gtinScansMetric, bonPrintsMetric,
} from './metrics.js';

// ─── Hilfsfunktionen ─────────────────────────────────────────────────────────

export function pickRandom(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export function pickRegisterId()    { return pickRandom(REGISTER_IDS); }
export function pickCashierId(vuId) { return CASHIER_IDS[(vuId - 1) % CASHIER_IDS.length]; }
export function pickPaymentMethod() { return pickRandom(PAYMENT_METHODS); }
export function pickGtin()          { return pickRandom(ARTICLE_GTINS); }

// ─── Artikel via GTIN scannen ─────────────────────────────────────────────────

export function scanArticleByGtin(token, gtin) {
    const res = http.get(`${STORE_URL}/api/article/gtin/${gtin}`, {
        ...authHeaders(token),
        tags: { endpoint: 'scan_gtin' },
    });

    check(res, { '[GTIN-Scan] Status 200': (r) => r.status === 200 });
    if (res.status !== 200) return null;

    gtinScansMetric.add(1);
    try { return res.json('id'); } catch (_) { return null; }
}

// ─── Artikel-Pool vorladen ────────────────────────────────────────────────────

export function preloadArticlePool(token) {
    const pool = [];
    const depositPool = [];

    console.log('[Articles] Lade Artikel-Pool via GTIN-Scan...');

    for (const gtin of ARTICLE_GTINS) {
        const res = http.get(`${STORE_URL}/api/article/gtin/${gtin}`, {
            ...authHeaders(token),
            tags: { endpoint: 'preload_article' },
        });
        if (res.status === 200) {
            try {
                const a = res.json();
                if (a && a.id) {
                    if (a.isDeposit) depositPool.push(a.id);
                    else pool.push(a.id);
                }
            } catch (_) {}
        }
    }

    if (pool.length === 0) {
        for (let id = 1; id <= 15 && pool.length < 15; id++) {
            const res = http.get(`${STORE_URL}/api/article/id/${id}`, {
                ...authHeaders(token), tags: { endpoint: 'preload_fallback' },
            });
            if (res.status === 200) {
                try { const a = res.json(); if (a?.id) pool.push(a.id); } catch (_) {}
            }
        }
    }

    console.log(`[Articles] Pool: ${pool.length} Artikel, ${depositPool.length} Pfand-Artikel`);
    return { articlePool: pool, depositPool };
}

// ─── Checkout ────────────────────────────────────────────────────────────────

export function checkout(token, registerId, cashierId, articlePool, opts = {}) {
    const articleCount   = opts.articleCount   ?? 18;
    const discountChance = opts.discountChance  ?? 0.33;
    const discountRates  = opts.discountRates   ?? [10, 30];
    const paymentMethod  = opts.paymentMethod   ?? pickPaymentMethod();
    const scanGtins      = opts.scanGtins       ?? false;

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
            articleAmount:   1,
            discountPercent: Math.random() < discountChance ? pickRandom(discountRates) : null,
        });
    }

    const res = http.post(`${STORE_URL}/api/receipt/checkout`, JSON.stringify({
        storeId: STORE_ID, registerId, cashierId, paymentMethod, lines,
    }), { ...authHeaders(token), tags: { endpoint: 'checkout' } });

    const ok = check(res, {
        '[Checkout] Status 201':          (r) => r.status === 201,
        '[Checkout] receiptId vorhanden': (r) => {
            try { return r.json('receiptId') != null; } catch (_) { return false; }
        },
    });

    if (!ok) { bonsFailedMetric.add(1); return null; }

    checkoutsMetric.add(1);
    bonsCreatedMetric.add(1);
    articlesAddedMetric.add(lines.length);
    if (lines.some((l) => l.discountPercent != null)) discountsMetric.add(1);

    try { return res.json('receiptId'); } catch (_) { return null; }
}

// ─── Bondruck: OPEN → PRINTED ────────────────────────────────────────────────

export function printReceipt(token, receiptId) {
    const res = http.post(`${STORE_URL}/api/receipt/${receiptId}/print`, null, {
        ...authHeaders(token),
        tags: { endpoint: 'print_receipt' },
        responseCallback: http.expectedStatuses(200, 409),
    });

    const ok = check(res, {
        '[Bondruck] Status 200 oder 409': (r) => r.status === 200 || r.status === 409,
        '[Bondruck] Kein 500':            (r) => r.status !== 500,
    });

    if (res.status === 200) bonPrintsMetric.add(1);
    return res.status;
}

// ─── Stornierung: OPEN → CANCELLED ───────────────────────────────────────────

export function cancelReceipt(token, receiptId) {
    const res = http.post(`${STORE_URL}/api/receipt/${receiptId}/cancel`, null, {
        ...authHeaders(token),
        tags: { endpoint: 'cancel_receipt' },
        responseCallback: http.expectedStatuses(200, 409),
    });

    check(res, {
        '[Storno] Status 200 oder 409': (r) => r.status === 200 || r.status === 409,
        '[Storno] Kein 500':            (r) => r.status !== 500,
    });

    if (res.status === 200) cancelsMetric.add(1);
    return res.status;
}

// ─── Voucher: Direktes Einlösen (ohne vorherigen Check) ─────────────────────
//
// Wird in allen 5 Testarten genutzt wenn ein Kassierer einen Voucher-Code
// scannt und sofort einlöst — ohne vorher zu prüfen ob er gültig ist.
// 200 = erfolgreich eingelöst
// 409 = bereits eingelöst oder abgelaufen (fachlich korrekt, kein Fehler)
// 404 = Voucher-Code existiert nicht

export function redeemVoucher(token) {
    if (!VOUCHER_REGULAR_CODES || VOUCHER_REGULAR_CODES.length === 0) return;

    const voucherCode = pickRandom(VOUCHER_REGULAR_CODES);

    const res = http.post(`${STORE_URL}/api/voucher/${voucherCode}/redeem`, null, {
        ...authHeaders(token),
        tags: { endpoint: 'voucher_redeem' },
        responseCallback: http.expectedStatuses(200, 404, 409),
    });

    check(res, {
        '[Voucher] 200, 404 oder 409': (r) => r.status === 200 || r.status === 404 || r.status === 409,
        '[Voucher] Kein 500':          (r) => r.status !== 500,
    });

    if (res.status === 200) voucherRedeemsMetric.add(1);
}

// ─── Voucher: Check + Einlösen (voller Kassierer-Flow) ─────────────────────
//
// Wird in runFullBon genutzt: Kassierer prüft zuerst ob Voucher gültig ist,
// dann löst er ein. Realistischer aber langsamer als redeemVoucher.

export function checkAndRedeemVoucher(token, voucherCode) {
    const checkRes = http.get(`${STORE_URL}/api/voucher/${voucherCode}`, {
        ...authHeaders(token), tags: { endpoint: 'voucher_check' },
    });
    if (checkRes.status !== 200) return { checked: false, redeemed: false };
    voucherChecksMetric.add(1);

    try {
        if (checkRes.json('redeemedAt') != null) return { checked: true, redeemed: false };
    } catch (_) {}

    const redeemRes = http.post(`${STORE_URL}/api/voucher/${voucherCode}/redeem`, null, {
        ...authHeaders(token),
        tags: { endpoint: 'voucher_redeem' },
        responseCallback: http.expectedStatuses(200, 409),
    });
    check(redeemRes, {
        '[Voucher] 200 oder 409': (r) => r.status === 200 || r.status === 409,
        '[Voucher] Kein 500':     (r) => r.status !== 500,
    });
    if (redeemRes.status === 200) voucherRedeemsMetric.add(1);
    return { checked: true, redeemed: redeemRes.status === 200 };
}

// ─── Pfandrückgabe ────────────────────────────────────────────────────────────

export function depositReturn(token, registerId, cashierId, depositPool) {
    const pool = depositPool?.length > 0 ? depositPool : [1];
    const res = http.post(`${STORE_URL}/api/receipt/checkout`, JSON.stringify({
        storeId: STORE_ID, registerId, cashierId,
        paymentMethod: pickPaymentMethod(),
        lines: [{ articleId: pickRandom(pool), articleAmount: 1, discountPercent: null }],
    }), { ...authHeaders(token), tags: { endpoint: 'deposit_return' } });

    const ok = check(res, { '[Pfand] Status 201': (r) => r.status === 201 });
    if (ok) {
        depositsMetric.add(1);
        try {
            const id = res.json('receiptId');
            if (id) printReceipt(token, id);
        } catch (_) {}
    }
    return ok;
}

// ─── Kompletter Bon-Workflow ──────────────────────────────────────────────────
//
// Reihenfolge:
//   1. Checkout
//   2. Stornierung (VOR dem Druck — danach wäre es ein 409)
//   3. Voucher einlösen (checkAndRedeemVoucher mit GET+POST)
//   4. Pfandrückgabe
//   5. Bondruck (immer letzter Schritt)

export function runFullBon(token, vuId, pools, opts = {}) {
    const cashierId     = pickCashierId(vuId);
    const registerId    = pickRegisterId();
    const depositChance = opts.depositChance ?? 0.25;
    const cancelChance  = opts.cancelChance  ?? 0.01;
    const voucherChance = opts.voucherChance ?? 0.10;

    const receiptId = checkout(token, registerId, cashierId, pools.articlePool, opts);
    if (!receiptId) return false;

    if (Math.random() < cancelChance) {
        cancelReceipt(token, receiptId);
        return true;
    }

    if (Math.random() < voucherChance && VOUCHER_REGULAR_CODES.length > 0) {
        checkAndRedeemVoucher(token, pickRandom(VOUCHER_REGULAR_CODES));
    }

    if (Math.random() < depositChance) {
        depositReturn(token, registerId, cashierId, pools.depositPool);
    }

    printReceipt(token, receiptId);
    return true;
}