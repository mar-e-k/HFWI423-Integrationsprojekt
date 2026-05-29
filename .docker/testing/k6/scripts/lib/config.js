export const ORCHESTRATOR_URL = __ENV.ORCHESTRATOR_URL || 'http://host.docker.internal:8080';

function parseNumberList(envValue, fallback) {
    if (!envValue) return fallback;
    const parsed = envValue.split(',')
        .map((v) => Number(v.trim()))
        .filter((v) => Number.isFinite(v));
    return parsed.length > 0 ? parsed : fallback;
}

// Fixed Topology targeting for singular Gateway Routing Validation
export const STORE_ID     = 1;
export const REGISTER_IDS = [1];
export const CASHIER_IDS  = parseNumberList(__ENV.CASHIER_IDS, [4, 5, 6]);

// Fixed Domain Enums
export const PAYMENT_METHODS = ['CASH', 'CARD', 'ONLINE'];
export const DISCOUNT_RATES   = Array.from({ length: 46 }, (_, i) => 5 + i);
export const LASTTEST_DISCOUNTS = [10, 30];

export const ARTICLE_GTINS = [
    '10000001', '10000002', '10000003', '10000004', '10000005',
    '10000006', '10000007', '10000008', '10000009', '10000010',
    '10000011', '10000012', '10000013', '10000014', '10000015'
];