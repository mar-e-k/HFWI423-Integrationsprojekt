import http from 'k6/http';
import {check} from 'k6';
import {authHeaders} from '../auth.js';
import {ORCHESTRATOR_URL, STORE_ID} from '../../lib/config.js';
import {gtinScansMetric} from '../metrics.js';

export function getAllArticles(token) {
    const res = http.get(`${ORCHESTRATOR_URL}/api/article`, {
        ...authHeaders(token, STORE_ID),
        tags: { endpoint: 'get_all_articles' }
    });

    if (res.status !== 200) {
        console.error(`Failed to fetch articles. Status: ${res.status}`);
        return [];
    }
    return res.json();
}

export function getArticleByGtin(token, gtin) {
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