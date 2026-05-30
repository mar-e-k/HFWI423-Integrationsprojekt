export const ORCHESTRATOR_URL = __ENV.ORCHESTRATOR_URL || 'http://host.docker.internal:8080';

function parseNumberList(envValue, fallback) {
    if (!envValue) return fallback;
    const parsed = envValue.split(',')
        .map((v) => Number(v.trim()))
        .filter((v) => Number.isFinite(v));
    return parsed.length > 0 ? parsed : fallback;
}

export const STORE_ID = 1;
export const REGISTER_IDS = [1];
export const CASHIER_IDS = parseNumberList(__ENV.CASHIER_IDS, [4, 5, 6]);

export const PAYMENT_METHODS = [
    'CASH',
    'CARD',
    'ONLINE'
];
export const OVERRIDE_REASONS = [
    'MANUAL_OVERRIDE',
    'PRICE_NOT_FOUND',
    'PROMOTIONAL_ADJUSTMENT',
    'SYSTEM_CORRECTION',
    'CUSTOMER_REQUEST',
    'OTHER'
];
export const DISCOUNT_RATES = Array.from({length: 46}, (_, i) => 5 + i);
export const STRESSTEST_DISCOUNTS = [10, 30];