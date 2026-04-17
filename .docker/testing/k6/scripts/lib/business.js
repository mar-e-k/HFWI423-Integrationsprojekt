// ═══════════════════════════════════════════════════════════════════════════
// lib/business.js – Vollständiger Kassierer-Workflow
//
// Fachlicher Ablauf (wie ein echter Kassierer):
//   1. Artikel scannen via GTIN   → GET  /api/article/gtin/{gtin}
//   2. Bon abschließen (Checkout) → POST /api/receipt/checkout
//   3. Bon drucken                → POST /api/receipt/{id}/print
//   4. Optional: Voucher prüfen   → GET  /api/voucher/{code}
//   5. Optional: Voucher einlösen → POST /api/voucher/{code}/redeem
//   6. Optional: Pfand            → POST /api/receipt/checkout (Pfand-Artikel)
//   7. Optional: Stornierung      → POST /api/receipt/{id}/cancel
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

/**
 * POST /api/receipt/checkout
 * Erstellt Bon + alle Positionen in EINER Transaktion.
 * Gibt die receiptId zurück oder null bei Fehler.
 */
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
        '[Checkout] receiptId vorhanden': (r) => { try { return r.json('receiptId') != null; } catch (_) { return false; } },
    });

    if (!ok) { bonsFailedMetric.add(1); return null; }

    checkoutsMetric.add(1);
    bonsCreatedMetric.add(1);
    articlesAddedMetric.add(lines.length);
    if (lines.some((l) => l.discountPercent != null)) discountsMetric.add(1);

    try { return res.json('receiptId'); } catch (_) { return null; }
}

// ─── Bondruck: OPEN → PRINTED ────────────────────────────────────────────────

/**
 * POST /api/receipt/{id}/print
 *
 * Druckt den Bon ab — letzter Schritt des Kassier-Workflows.
 * 200 = erfolgreich gedruckt
 * 409 = bereits gedruckt oder storniert
 * 404 = nicht gefunden
 */
export function printReceipt(token, receiptId) {
    const res = http.post(`${STORE_URL}/api/receipt/${receiptId}/print`, null, {
        ...authHeaders(token),
        tags: { endpoint: 'print_receipt' },
    });

    const ok = check(res, {
        '[Bondruck] Status 200 oder 409': (r) => r.status === 200 || r.status === 409,
        '[Bondruck] Kein 500':            (r) => r.status !== 500,
    });

    if (res.status === 200) bonPrintsMetric.add(1);
    return res.status;
}

// ─── Stornierung: OPEN → CANCELLED ───────────────────────────────────────────

/**
 * POST /api/receipt/{id}/cancel
 *
 * Storniert den Bon.
 * 200 = erfolgreich storniert
 * 409 = bereits storniert oder bereits gedruckt
 * 404 = nicht gefunden
 */
export function cancelReceipt(token, receiptId) {
    const res = http.post(`${STORE_URL}/api/receipt/${receiptId}/cancel`, null, {
        ...authHeaders(token),
        tags: { endpoint: 'cancel_receipt' },
    });

    check(res, {
        '[Storno] Status 200 oder 409': (r) => r.status === 200 || r.status === 409,
        '[Storno] Kein 500':            (r) => r.status !== 500,
    });

    if (res.status === 200) cancelsMetric.add(1);
    return res.status;
}

// ─── Voucher prüfen + einlösen ────────────────────────────────────────────────

export function checkAndRedeemVoucher(token, voucherCode) {
    const checkRes = http.get(`${STORE_URL}/api/voucher/${voucherCode}`, {
        ...authHeaders(token), tags: { endpoint: 'voucher_check' },
    });
    if (checkRes.status !== 200) return { checked: false, redeemed: false };
    voucherChecksMetric.add(1);

    try { if (checkRes.json('redeemedAt') != null) return { checked: true, redeemed: false }; } catch (_) {}

    const redeemRes = http.post(`${STORE_URL}/api/voucher/${voucherCode}/redeem`, null, {
        ...authHeaders(token), tags: { endpoint: 'voucher_redeem' },
    });
    check(redeemRes, {
        '[Voucher] 200 oder 409': (r) => r.status === 200 || r.status === 409,
        '[Voucher] Kein 500':     (r) => r.status !== 500,
    });
    if (redeemRes.status === 200) voucherRedeemsMetric.add(1);
    return { checked: true, redeemed: redeemRes.status === 200 };
}

export function redeemVoucher(token, voucherCode) {
    const res = http.post(`${STORE_URL}/api/voucher/${voucherCode}/redeem`, null, {
        ...authHeaders(token), tags: { endpoint: 'voucher_race_redeem' },
    });
    check(res, {
        '[Race] 200 oder 409 (kein 500!)': (r) => r.status === 200 || r.status === 409,
        '[Race] Kein DB-Fehler':           (r) => r.status !== 500,
    });
    if (res.status === 200) voucherRedeemsMetric.add(1);
    return res.status;
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
        // Pfandbon direkt drucken
        try {
            const id = res.json('receiptId');
            if (id) printReceipt(token, id);
        } catch (_) {}
    }
    return ok;
}

// ─── Kompletter Bon-Workflow ──────────────────────────────────────────────────

/**
 * Vollständiger Kassierer-Ablauf:
 *   1. Checkout (mit optionalem GTIN-Scan)
 *   2. Bondruck (immer — jeder Bon wird abgeschlossen)
 *   3. Optional: Voucher prüfen + einlösen
 *   4. Optional: Pfandrückgabe
 *   5. Optional: Stornierung (nur wenn nicht gedruckt — daher zuerst prüfen)
 *
 * Fachliche Besonderheit: Stornierung passiert VOR dem Druck.
 * Bons die gedruckt wurden können nicht mehr storniert werden (→ 409).
 */
export function runFullBon(token, vuId, pools, opts = {}) {
    const cashierId    = pickCashierId(vuId);
    const registerId   = pickRegisterId();
    const depositChance = opts.depositChance ?? 0.25;
    const cancelChance  = opts.cancelChance  ?? 0.01;
    const voucherChance = opts.voucherChance ?? 0.10;

    // 1) Checkout
    const receiptId = checkout(token, registerId, cashierId, pools.articlePool, opts);
    if (!receiptId) return false;

    // 2) Stornierung kommt VOR dem Druck (sonst → 409)
    if (Math.random() < cancelChance) {
        cancelReceipt(token, receiptId);
        return true;   // stornierte Bons werden nicht gedruckt
    }

    // 3) Optional: Voucher einlösen
    if (Math.random() < voucherChance && VOUCHER_REGULAR_CODES.length > 0) {
        checkAndRedeemVoucher(token, pickRandom(VOUCHER_REGULAR_CODES));
    }

    // 4) Optional: Pfandrückgabe
    if (Math.random() < depositChance) {
        depositReturn(token, registerId, cashierId, pools.depositPool);
    }

    // 5) Bondruck — immer der letzte Schritt
    printReceipt(token, receiptId);

    return true;
}