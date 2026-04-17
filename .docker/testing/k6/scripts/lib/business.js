// ═══════════════════════════════════════════════════════════════════════════
// lib/business.js – Business-Logik: Bon, Artikel, Rabatt, Pfand, Stornierung
// ═══════════════════════════════════════════════════════════════════════════

import http from 'k6/http';
import { check } from 'k6';

import {
    STORE_URL, STORE_ID, REGISTER_IDS,
    NORMAL_ARTICLE_IDS, DEPOSIT_ARTICLE_IDS,
    PAYMENT_METHODS,
} from './config.js';
import { authHeaders } from './auth.js';
import {
    bonsCreatedMetric, bonsFailedMetric,
    articlesAddedMetric, cancelsMetric,
    depositsMetric, discountsMetric,
} from './metrics.js';

// ─── Hilfsfunktionen ────────────────────────────────────────────────────────

export function pickRandom(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export function pickRegisterId() {
    return pickRandom(REGISTER_IDS);
}

export function pickPaymentMethod() {
    return pickRandom(PAYMENT_METHODS);
}

export function pickArticleId() {
    return pickRandom(NORMAL_ARTICLE_IDS);
}

export function pickDepositArticleId() {
    return pickRandom(DEPOSIT_ARTICLE_IDS);
}

// ─── Bon-Erstellung ─────────────────────────────────────────────────────────

/**
 * Legt einen neuen Bon an.
 *
 * POST /api/receipt/
 * Body: { storeId, registerId, cashierId, paymentMethod, status:null, isDepositReturn:false }
 *
 * Gibt die Receipt-ID zurück oder null wenn fehlgeschlagen.
 */
export function createReceipt(token, registerId, cashierId, paymentMethod) {
    const body = JSON.stringify({
        storeId:        STORE_ID,
        registerId:     registerId,
        cashierId:      cashierId,
        paymentMethod:  paymentMethod,
        status:         null,
        isDepositReturn: false,
    });

    const res = http.post(`${STORE_URL}/api/receipt/`, body, {
        ...authHeaders(token),
        tags: { endpoint: 'create_receipt' },
    });

    const ok = check(res, {
        '[Bon] Status 200/201': (r) => r.status === 200 || r.status === 201,
        '[Bon] ID in Response':  (r) => r.json('id') !== undefined || r.json('receiptId') !== undefined,
    });

    if (!ok) {
        bonsFailedMetric.add(1);
        return null;
    }

    bonsCreatedMetric.add(1);
    return res.json('id') || res.json('receiptId');
}

// ─── Artikel hinzufügen ─────────────────────────────────────────────────────

/**
 * Fügt eine Artikelposition zum Bon hinzu.
 *
 * POST /api/receipt/{receiptId}/line
 * Body: { articleId, quantity, ... }
 */
export function addArticleLine(token, receiptId, articleId, quantity = 1) {
    const body = JSON.stringify({
        articleId: articleId,
        quantity:  quantity,
    });

    const res = http.post(`${STORE_URL}/api/receipt/${receiptId}/line`, body, {
        ...authHeaders(token),
        tags: { endpoint: 'add_article' },
    });

    const ok = check(res, {
        '[Artikel] Status 200/201': (r) => r.status === 200 || r.status === 201,
    });

    if (ok) articlesAddedMetric.add(1);
    return ok;
}

// ─── Rabatt anwenden ────────────────────────────────────────────────────────

/**
 * Wendet einen prozentualen Rabatt auf einen Bon an.
 *
 * PATCH /api/receipt/{receiptId}/discount
 * Body: { discountPercent }
 *
 * Hinweis: Falls der Rabatt-Endpunkt anders heißt, ist das nicht-kritisch —
 * wir zählen den Fehler als "Rabatt-Versuch" und laufen weiter.
 */
export function applyDiscount(token, receiptId, discountPercent) {
    const body = JSON.stringify({ discountPercent });

    const res = http.patch(`${STORE_URL}/api/receipt/${receiptId}/discount`, body, {
        ...authHeaders(token),
        tags: { endpoint: 'apply_discount' },
    });

    // Rabatt-Endpunkt kann 200, 201 oder 204 liefern
    const ok = check(res, {
        '[Rabatt] Status 2xx': (r) => r.status >= 200 && r.status < 300,
    });

    if (ok) discountsMetric.add(1);
    return ok;
}

// ─── Pfandrückgabe ──────────────────────────────────────────────────────────

/**
 * Erstellt eine Pfandrückgabe-Transaktion.
 *
 * POST /api/receipt/{storeId}/{registerId}/{cashierId}/deposit-return
 * Body: { storeId, registerId, cashierId, paymentMethod, status:null, isDepositReturn:true }
 */
export function depositReturn(token, registerId, cashierId, paymentMethod) {
    const body = JSON.stringify({
        storeId:         STORE_ID,
        registerId:      registerId,
        cashierId:       cashierId,
        paymentMethod:   paymentMethod,
        status:          null,
        isDepositReturn: true,
    });

    const url = `${STORE_URL}/api/receipt/${STORE_ID}/${registerId}/${cashierId}/deposit-return`;

    const res = http.post(url, body, {
        ...authHeaders(token),
        tags: { endpoint: 'deposit_return' },
    });

    const ok = check(res, {
        '[Pfand] Status 2xx': (r) => r.status >= 200 && r.status < 300,
    });

    if (ok) depositsMetric.add(1);
    return ok;
}

// ─── Stornierung ────────────────────────────────────────────────────────────

/**
 * Storniert einen Bon.
 *
 * POST /api/receipt/{receiptId}/cancel
 */
export function cancelReceipt(token, receiptId) {
    const res = http.post(`${STORE_URL}/api/receipt/${receiptId}/cancel`, null, {
        ...authHeaders(token),
        tags: { endpoint: 'cancel_receipt' },
    });

    const ok = check(res, {
        '[Storno] Status 2xx': (r) => r.status >= 200 && r.status < 300,
    });

    if (ok) cancelsMetric.add(1);
    return ok;
}

// ─── Kompletter Bon-Workflow ────────────────────────────────────────────────

/**
 * Führt einen kompletten Bon aus: erstellen, Artikel hinzufügen, optional
 * Rabatt, optional Pfand, optional Storno.
 *
 * @param {string}  token           - JWT-Token
 * @param {number}  cashierId       - Kassierer-ID aus Rotation
 * @param {object}  opts            - { articleCount, discountChance, discountRates, depositChance, cancelChance, paymentMethod, registerId }
 */
export function runFullBon(token, cashierId, opts) {
    const registerId     = opts.registerId     || pickRegisterId();
    const paymentMethod  = opts.paymentMethod  || pickPaymentMethod();
    const articleCount   = opts.articleCount   || 18;
    const discountChance = opts.discountChance != null ? opts.discountChance : 0.33;
    const discountRates  = opts.discountRates  || [10, 30];
    const depositChance  = opts.depositChance  != null ? opts.depositChance  : 0.25;
    const cancelChance   = opts.cancelChance   != null ? opts.cancelChance   : 0.01;

    // 1) Bon erstellen
    const receiptId = createReceipt(token, registerId, cashierId, paymentMethod);
    if (!receiptId) return false;

    // 2) Artikel hinzufügen
    for (let i = 0; i < articleCount; i++) {
        addArticleLine(token, receiptId, pickArticleId(), 1);
    }

    // 3) Rabatt
    if (Math.random() < discountChance) {
        applyDiscount(token, receiptId, pickRandom(discountRates));
    }

    // 4) Pfandrückgabe als separate Transaktion
    if (Math.random() < depositChance) {
        depositReturn(token, registerId, cashierId, paymentMethod);
    }

    // 5) Stornierung
    if (Math.random() < cancelChance) {
        cancelReceipt(token, receiptId);
    }

    return true;
}