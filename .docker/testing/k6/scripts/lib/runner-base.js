import http from 'k6/http';
import {authHeaders, setupAuth} from './auth.js';
import {ORCHESTRATOR_URL, STORE_ID} from './config.js';
import {getAllArticles} from "./client/article.js";
import {getAllVouchers} from "./client/voucher.js";

export function executeSharedSetup(scenarioName) {
    const token = setupAuth();
    assertStoreReachable(token);

    const articles = getAllArticles(token);
    const vouchers = getAllVouchers(token);

    const articlePool = [];
    const depositPool = [];
    const gtinPool = [];
    const voucherPool = [];

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
    
    vouchers.forEach((item) => {
        if (item?.code) {
            voucherPool.push(item.code);
        }
    });

    return {
        token,
        pools: {articlePool, depositPool, gtinPool, voucherPool}
    };
}

export function executeSharedSummary(data, scenarioName) {
    const now = new Date();
    const date = now.toISOString().split('T')[0];

    const jsonFileName = `vendix-${date}-${scenarioName}.json`;

    return {
        [`/etc/k6/reports/${jsonFileName}`]: JSON.stringify(data, null, 2)
    };
}

function assertStoreReachable(token) {
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

export const baseThresholds = {
    'http_req_duration': ['p(95)<2000'],
    'http_req_failed': ['rate<0.05'],
    'checks': ['rate>0.95'],
};