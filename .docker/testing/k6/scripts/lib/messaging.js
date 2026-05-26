// ═══════════════════════════════════════════════════════════════════════════
// lib/messaging.js – k6-Hilfsfunktionen für den Messaging-E2E-Lasttest
// ═══════════════════════════════════════════════════════════════════════════

import http from 'k6/http';
import { check } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import { sleep } from 'k6';
import { authHeaders } from './auth.js';
import { STORE_URL, STORE_ID } from './config.js';

// ── Custom Metriken ──────────────────────────────────────────────────────────
export const ordersTriggeredMetric       = new Counter('vendix_messaging_orders_triggered');
export const urgentOrdersTriggeredMetric = new Counter('vendix_messaging_urgent_orders_triggered');
export const orderErrorsMetric           = new Counter('vendix_messaging_order_errors');
export const stockCheckMetric            = new Counter('vendix_messaging_stock_checks');
export const stockCheckFailedMetric      = new Counter('vendix_messaging_stock_check_failed');
export const orderLatencyTrend           = new Trend('vendix_messaging_order_latency_ms', true);
export const replenishmentAcceptedMetric = new Counter('vendix_replenishment_orders_accepted');
export const replenishmentFailedMetric   = new Counter('vendix_replenishment_orders_failed');
export const replenishmentAckTrend       = new Trend('vendix_replenishment_ack_ms', true);
export const replenishmentE2ETrend       = new Trend('vendix_replenishment_e2e_ms', true);

function pickRandom(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

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

// ── createReplenishmentOrder ────────────────────────────────────────────────

export function createReplenishmentOrder(token, articleId, amount, urgent = false) {
    const payload = JSON.stringify({
        storeId: STORE_ID,
        articleId,
        amount,
        urgent,
    });

    const res = http.post(`${STORE_URL}/api/replenishment-orders`, payload, {
        ...authHeaders(token),
        tags: { endpoint: 'replenishment_ack' },
    });

    replenishmentAckTrend.add(res.timings.duration);

    const ok = check(res, {
        '[Replenishment] HTTP 202': (r) => r.status === 202,
        '[Replenishment] correlationId vorhanden': (r) => {
            try { return r.json('correlationId') != null; } catch { return false; }
        },
    });

    if (!ok) {
        replenishmentFailedMetric.add(1);
        console.error(`[Replenishment] Fehler: status=${res.status} body=${res.body}`);
        return null;
    }

    replenishmentAcceptedMetric.add(1);
    return res.json();
}

// ── getReplenishmentOrderStatus ─────────────────────────────────────────────

export function getReplenishmentOrderStatus(token, correlationId) {
    const res = http.get(`${STORE_URL}/api/replenishment-orders/${correlationId}`, {
        ...authHeaders(token),
        tags: { endpoint: 'replenishment_status' },
    });

    const ok = check(res, {
        '[ReplenishmentStatus] HTTP 200': (r) => r.status === 200,
        '[ReplenishmentStatus] status vorhanden': (r) => {
            try { return r.json('status') != null; } catch { return false; }
        },
    });

    if (!ok) {
        replenishmentFailedMetric.add(1);
        return null;
    }

    return res.json();
}

// ── runAsyncReplenishment ───────────────────────────────────────────────────

export function runAsyncReplenishment(token, articlePool, opts = {}) {
    const pool = articlePool?.length > 0 ? articlePool : [1, 2, 3, 4, 5];
    const articleId = opts.articleId ?? pickRandom(pool);
    const amount = opts.amount ?? 10;
    const urgent = opts.urgent ?? Math.random() < 0.2;
    const maxPolls = opts.maxPolls ?? 3;
    const pollIntervalSeconds = opts.pollIntervalSeconds ?? 1;

    const startedAt = Date.now();
    const ack = createReplenishmentOrder(token, articleId, amount, urgent);
    if (!ack?.correlationId) {
        return false;
    }

    for (let i = 0; i < maxPolls; i++) {
        const status = getReplenishmentOrderStatus(token, ack.correlationId);
        if (status?.status === 'RECEIVED') {
            replenishmentE2ETrend.add(Date.now() - startedAt);
            return true;
        }
        if (status?.status === 'FAILED') {
            replenishmentFailedMetric.add(1);
            return false;
        }
        sleep(pollIntervalSeconds);
    }

    return true;
}
