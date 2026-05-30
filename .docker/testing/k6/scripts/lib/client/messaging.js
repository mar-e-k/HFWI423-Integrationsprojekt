import http from 'k6/http';
import {check} from 'k6';
import {authHeaders} from '../auth.js';
import {ORCHESTRATOR_URL, STORE_ID} from '../config.js';
import {Trend, Counter} from 'k6/metrics';

const replenishmentAckTrend = new Trend('vendix_replenishment_ack_ms');
const ordersFailedCounter = new Counter('vendix_replenishment_orders_failed');
const stockCheckFailedCounter = new Counter('vendix_messaging_stock_check_failed');

export function getStoreStock(token, articleId) {
    const res = http.get(`${ORCHESTRATOR_URL}/api/store-stock/article/${articleId}`, {
        ...authHeaders(token, STORE_ID),
        tags: {endpoint: 'get_store_stock'},
    });

    if (res.status !== 200) {
        stockCheckFailedCounter.add(1);
        return null;
    }

    try {
        return res.json();
    } catch (_) {
        stockCheckFailedCounter.add(1);
        return null;
    }
}

export function createReplenishmentOrder(token, articleId, amount, isUrgent) {
    const payload = JSON.stringify({
        storeId: STORE_ID,
        articleId: articleId,
        amount: amount,
        urgent: isUrgent
    });

    const res = http.post(`${ORCHESTRATOR_URL}/api/store-stock-order`, payload, {
        ...authHeaders(token, STORE_ID),
        tags: {endpoint: 'create_store_stock_order'},
    });

    const isOk = check(res, {
        'Store stock order accepted': (r) => r.status === 202 || r.status === 201 || r.status === 200,
    });

    if (!isOk) {
        ordersFailedCounter.add(1);
        return null;
    }

    replenishmentAckTrend.add(res.timings.duration);

    try {
        return res.json();
    } catch (_) {
        return null;
    }
}

export function getReplenishmentOrderStatus(token, correlationId) {
    const res = http.get(`${ORCHESTRATOR_URL}/api/store-stock-order/correlation-id/${correlationId}`, {
        ...authHeaders(token, STORE_ID),
        tags: {endpoint: 'get_store_stock_order_status'},
    });

    if (res.status !== 200) return null;

    try {
        return res.json();
    } catch (_) {
        return null;
    }
}
