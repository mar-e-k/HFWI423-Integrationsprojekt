import http from 'k6/http';
import {check} from 'k6';
import {authHeaders} from '../auth.js';
import {ORCHESTRATOR_URL, STORE_ID} from '../config.js';
import {gtinScansMetric} from '../metrics.js';

export function assertStoreReachable(token) {
    const res = http.get(`${ORCHESTRATOR_URL}/actuator/health`, {
        ...authHeaders(token, STORE_ID),
        tags: {endpoint: 'preflight_store'},
        timeout: '5s',
        responseCallback: http.expectedStatuses(200, 401, 403, 404),
    });

    if (res.error || res.status === 0 || res.status >= 500) {
        throw new Error(`[Preflight] Gateway path to Store offline or broken: ${res.error || res.status}`);
    }
}

export function scanArticleByGtin(token, gtin) {
    const res = http.get(`${ORCHESTRATOR_URL}/api/article/gtin/${gtin}`, {
        ...authHeaders(token, STORE_ID),
        tags: {endpoint: 'scan_gtin'},
    });

    if (!check(res, {'GTIN scan returns 200': (r) => r.status === 200})) return null;

    gtinScansMetric.add(1);
    try {
        return res.json('id');
    } catch (_) {
        return null;
    }
}

export function getArticleById(token, id) {
    const res = http.get(`${ORCHESTRATOR_URL}/api/article/id/${id}`, {
        ...authHeaders(token, STORE_ID),
        tags: {endpoint: 'get_article_by_id'},
    });

    return check(res, {'Get article by ID returns 200': (r) => r.status === 200})
        ? res.json()
        : null;
}

export function preloadArticlePool(token) {
    const articlePool = [];
    const depositPool = [];
    const gtinPool = [];

    const res = http.get(`${ORCHESTRATOR_URL}/api/article`, {
        ...authHeaders(token, STORE_ID),
        tags: {endpoint: 'preload_articles_bulk'},
    });

    if (res.status !== 200) {
        console.error(`[Articles] Response yielded: ${res.body}`)
        throw new Error(`[Articles] Failed bulk fetch from gateway context: ${res.status} - ${res.body}`);
    }

    try {
        const articles = res.json();

        if (!Array.isArray(articles) || articles.length === 0) {
            throw new Error('Article array returned from database is empty. Ensure data seeding is completed.');
        }

        articles.forEach((item) => {
            if (item?.id) {
                if (item.isDeposit) {
                    depositPool.push(item.id);
                } else {
                    articlePool.push(item.id);
                }

                if (item.gtin) {
                    gtinPool.push(item.gtin);
                }
            }
        });

    } catch (e) {
        throw new Error(`[Articles] Parsing error during setup data pooling: ${e.message}`);
    }

    if (articlePool.length === 0) {
        throw new Error(`[Articles] Seeding pool arrays failed. Zero items processed from target: ${ORCHESTRATOR_URL}`);
    }

    return {articlePool, depositPool, gtinPool};
}