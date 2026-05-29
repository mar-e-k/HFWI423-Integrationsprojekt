import http from 'k6/http';
import { check } from 'k6';
import { authHeaders } from '../auth.js';
import { ORCHESTRATOR_URL, ARTICLE_GTINS } from '../config.js';
import { gtinScansMetric } from '../metrics.js';

export function assertStoreReachable(token) {
    const res = http.get(`${ORCHESTRATOR_URL}/actuator/health`, {
        ...authHeaders(token),
        tags: { endpoint: 'preflight_store' },
        timeout: '5s',
        responseCallback: http.expectedStatuses(200, 401, 403, 404),
    });

    if (res.error || res.status === 0 || res.status >= 500) {
        throw new Error(`[Preflight] Gateway path to Store offline or broken: ${res.error || res.status}`);
    }
}

export function scanArticleByGtin(token, gtin) {
    const res = http.get(`${ORCHESTRATOR_URL}/api/article/gtin/${gtin}`, {
        ...authHeaders(token),
        tags: { endpoint: 'scan_gtin' },
    });

    if (!check(res, { 'GTIN scan returns 200': (r) => r.status === 200 })) return null;

    gtinScansMetric.add(1);
    try { return res.json('id'); } catch (_) { return null; }
}

export function preloadArticlePool(token) {
    const pool = [];
    const depositPool = [];

    for (const gtin of ARTICLE_GTINS) {
        const res = http.get(`${ORCHESTRATOR_URL}/api/article/gtin/${gtin}`, {
            ...authHeaders(token),
            tags: { endpoint: 'preload_article' },
        });
        if (res.status === 200) {
            try {
                const item = res.json();
                if (item?.id) {
                    item.isDeposit ? depositPool.push(item.id) : pool.push(item.id);
                }
            } catch (_) {}
        }
    }

    if (pool.length === 0) {
        throw new Error(`[Articles] Failed to seed pool arrays via Gateway from target: ${ORCHESTRATOR_URL}`);
    }
    return { articlePool: pool, depositPool };
}