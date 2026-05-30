import {setupAuth} from './auth.js';
import {assertStoreReachable, preloadArticlePool} from './client/article.js';

export function executeSharedSetup(scenarioName) {
    const token = setupAuth();
    assertStoreReachable(token);
    const {articlePool, depositPool} = preloadArticlePool(token);

    return {
        token,
        pools: {articlePool, depositPool}
    };
}

export function executeSharedSummary(data, scenarioName) {
    const now = new Date();
    const date = now.toISOString().split('T')[0];
    const fileName = `vendix-${date}-${scenarioName}.json`;

    return {
        [`/etc/k6/reports/${fileName}`]: JSON.stringify(data, null, 2),
    };
}

export const baseThresholds = {
    'http_req_duration': ['p(95)<2000'],
    'http_req_failed': ['rate<0.05'],
    'checks': ['rate>0.95'],
};
