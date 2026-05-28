import { setupAuth } from './auth.js';
import { assertStoreReachable, preloadArticlePool } from './business.js';
import { ORCHESTRATOR_URL, STORE_URL } from './config.js';

export function executeSharedSetup(scenarioName) {
    const token = setupAuth();
    assertStoreReachable(token);
    const { articlePool, depositPool } = preloadArticlePool(token);

    return {
        token,
        pools: { articlePool, depositPool }
    };
}

export function executeSharedTeardown(scenarioName) {
    // Shared logging, cleanup, or metric reporting hook
}

export const baseThresholds = {
    'http_req_duration': ['p(95)<2000'],
    'http_req_failed': ['rate<0.05'],
    'checks': ['rate>0.95'],
};