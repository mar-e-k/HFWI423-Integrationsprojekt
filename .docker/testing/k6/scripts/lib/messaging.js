// ═══════════════════════════════════════════════════════════════════════════
// lib/messaging.js – k6-Hilfsfunktionen für den Messaging-E2E-Lasttest
// ═══════════════════════════════════════════════════════════════════════════

import http from 'k6/http';
import { check } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import { authHeaders } from './auth.js';
import { STORE_URL, STORE_ID } from './config.js';

// ── Custom Metriken ──────────────────────────────────────────────────────────
export const ordersTriggeredMetric       = new Counter('vendix_messaging_orders_triggered');
export const urgentOrdersTriggeredMetric = new Counter('vendix_messaging_urgent_orders_triggered');
export const orderErrorsMetric           = new Counter('vendix_messaging_order_errors');
export const stockCheckMetric            = new Counter('vendix_messaging_stock_checks');
export const stockCheckFailedMetric      = new Counter('vendix_messaging_stock_check_failed');
export const orderLatencyTrend           = new Trend('vendix_messaging_order_latency_ms', true);

// ── triggerOrder ─────────────────────────────────────────────────────────────

export function triggerOrder(token, articleId, amount) {
    const payload = JSON.stringify({
        storeId:   STORE_ID,
        articleId: articleId,
        amount:    amount,
    });

    const res = http.post(
        `${STORE_URL}/api/test/order`,
        payload,
        authHeaders(token)
    );

    orderLatencyTrend.add(res.timings.duration);

    const ok = check(res, {
        '[Order] HTTP 200': (r) => r.status === 200,
        '[Order] status=sent': (r) => {
            try { return r.json('status') === 'sent'; } catch { return false; }
        },
    });

    if (ok) {
        ordersTriggeredMetric.add(1);
    } else {
        orderErrorsMetric.add(1);
        console.error(`[Order] Fehler: status=${res.status} body=${res.body}`);
    }

    return ok ? res.json() : null;
}

// ── triggerUrgentOrder ───────────────────────────────────────────────────────

export function triggerUrgentOrder(token, articleId, amount) {
    const payload = JSON.stringify({
        storeId:   STORE_ID,
        articleId: articleId,
        amount:    amount,
    });

    const res = http.post(
        `${STORE_URL}/api/test/order/urgent`,
        payload,
        authHeaders(token)
    );

    orderLatencyTrend.add(res.timings.duration);

    const ok = check(res, {
        '[UrgentOrder] HTTP 200': (r) => r.status === 200,
        '[UrgentOrder] status=sent': (r) => {
            try { return r.json('status') === 'sent'; } catch { return false; }
        },
    });

    if (ok) {
        urgentOrdersTriggeredMetric.add(1);
    } else {
        orderErrorsMetric.add(1);
        console.error(`[UrgentOrder] Fehler: status=${res.status} body=${res.body}`);
    }

    return ok ? res.json() : null;
}

// ── getStoreStock ────────────────────────────────────────────────────────────

export function getStoreStock(token, articleId) {
    const res = http.get(
        `${STORE_URL}/api/test/store-stock?storeId=${STORE_ID}&articleId=${articleId}`,
        authHeaders(token)
    );

    stockCheckMetric.add(1);

    const ok = check(res, {
        '[StoreStock] HTTP 200': (r) => r.status === 200,
    });

    if (!ok) {
        stockCheckFailedMetric.add(1);
        console.warn(`[StoreStock] Nicht gefunden oder Fehler: status=${res.status} articleId=${articleId}`);
        return null;
    }

    return res.json();
}