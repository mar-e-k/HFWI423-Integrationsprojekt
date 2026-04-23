// ═══════════════════════════════════════════════════════════════════════════
// lib/config.js – Zentrale Konfiguration für alle k6-Lasttests
// ═══════════════════════════════════════════════════════════════════════════

export const ORCHESTRATOR_URL = __ENV.ORCHESTRATOR_URL || 'http://host.docker.internal:8080';
export const STORE_URL        = __ENV.STORE_URL        || 'http://host.docker.internal:8081';

function parseIdList(envValue, fallback) {
    if (!envValue) return fallback;
    const parsed = envValue.split(',').map((v) => v.trim()).filter(Boolean);
    return parsed.length > 0 ? parsed : fallback;
}

// Auth
export const ADMIN_USERNAME = __ENV.ADMIN_USERNAME || 'admin.three';
export const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || 'admin';

// Store-Konfiguration
export const STORE_ID     = Number(__ENV.STORE_ID || 1);
export const REGISTER_IDS = [1, 2, 3];
export const CASHIER_IDS  = [4, 5, 6]; // cashier.one/two/three

// ── Artikel-GTINs (aus import.sql) ──────────────────────────────────────────
export const ARTICLE_GTINS = [
    '10000001', // Apfel
    '10000002', // Birne
    '10000003', // Banane
    '10000004', // Orange
    '10000005', // Traube
    '10000006', // Mango
    '10000007', // Ananas
    '10000008', // Kiwi
    '10000009', // Erdbeere
    '10000010', // Blaubeere
    '10000011', // Pfirsich
    '10000012', // Pflaume
    '10000013', // Zitrone
    '10000014', // Limette
    '10000015', // Wassermelone
];

// Artikel-IDs — Fallback wenn GTIN-Scan fehlschlägt
export let NORMAL_ARTICLE_IDS   = Array.from({ length: 15 }, (_, i) => i + 1);
export const DEPOSIT_ARTICLE_IDS = parseIdList(__ENV.DEPOSIT_ARTICLE_IDS, []);

// ── Voucher-Codes (aus import.sql geseedet) ──────────────────────────────────
// 100 Codes damit unter paralleler Last (200 VUs) keine Kollisionen entstehen
export const VOUCHER_REGULAR_CODES = [
    'bbbbbbbb-0000-0000-0000-000000000001',
    'bbbbbbbb-0000-0000-0000-000000000002',
    'bbbbbbbb-0000-0000-0000-000000000003',
    'bbbbbbbb-0000-0000-0000-000000000004',
    'bbbbbbbb-0000-0000-0000-000000000005',
    'cccccccc-0000-0000-0000-000000000001',
    'cccccccc-0000-0000-0000-000000000002',
    'cccccccc-0000-0000-0000-000000000003',
    'cccccccc-0000-0000-0000-000000000004',
    'cccccccc-0000-0000-0000-000000000005',
    'cccccccc-0000-0000-0000-000000000006',
    'cccccccc-0000-0000-0000-000000000007',
    'cccccccc-0000-0000-0000-000000000008',
    'cccccccc-0000-0000-0000-000000000009',
    'cccccccc-0000-0000-0000-000000000010',
    'cccccccc-0000-0000-0000-000000000011',
    'cccccccc-0000-0000-0000-000000000012',
    'cccccccc-0000-0000-0000-000000000013',
    'cccccccc-0000-0000-0000-000000000014',
    'cccccccc-0000-0000-0000-000000000015',
    'cccccccc-0000-0000-0000-000000000016',
    'cccccccc-0000-0000-0000-000000000017',
    'cccccccc-0000-0000-0000-000000000018',
    'cccccccc-0000-0000-0000-000000000019',
    'cccccccc-0000-0000-0000-000000000020',
    'cccccccc-0000-0000-0000-000000000021',
    'cccccccc-0000-0000-0000-000000000022',
    'cccccccc-0000-0000-0000-000000000023',
    'cccccccc-0000-0000-0000-000000000024',
    'cccccccc-0000-0000-0000-000000000025',
    'cccccccc-0000-0000-0000-000000000026',
    'cccccccc-0000-0000-0000-000000000027',
    'cccccccc-0000-0000-0000-000000000028',
    'cccccccc-0000-0000-0000-000000000029',
    'cccccccc-0000-0000-0000-000000000030',
    'cccccccc-0000-0000-0000-000000000031',
    'cccccccc-0000-0000-0000-000000000032',
    'cccccccc-0000-0000-0000-000000000033',
    'cccccccc-0000-0000-0000-000000000034',
    'cccccccc-0000-0000-0000-000000000035',
    'cccccccc-0000-0000-0000-000000000036',
    'cccccccc-0000-0000-0000-000000000037',
    'cccccccc-0000-0000-0000-000000000038',
    'cccccccc-0000-0000-0000-000000000039',
    'cccccccc-0000-0000-0000-000000000040',
    'cccccccc-0000-0000-0000-000000000041',
    'cccccccc-0000-0000-0000-000000000042',
    'cccccccc-0000-0000-0000-000000000043',
    'cccccccc-0000-0000-0000-000000000044',
    'cccccccc-0000-0000-0000-000000000045',
    'cccccccc-0000-0000-0000-000000000046',
    'cccccccc-0000-0000-0000-000000000047',
    'cccccccc-0000-0000-0000-000000000048',
    'cccccccc-0000-0000-0000-000000000049',
    'cccccccc-0000-0000-0000-000000000050',
    'cccccccc-0000-0000-0000-000000000051',
    'cccccccc-0000-0000-0000-000000000052',
    'cccccccc-0000-0000-0000-000000000053',
    'cccccccc-0000-0000-0000-000000000054',
    'cccccccc-0000-0000-0000-000000000055',
    'cccccccc-0000-0000-0000-000000000056',
    'cccccccc-0000-0000-0000-000000000057',
    'cccccccc-0000-0000-0000-000000000058',
    'cccccccc-0000-0000-0000-000000000059',
    'cccccccc-0000-0000-0000-000000000060',
    'cccccccc-0000-0000-0000-000000000061',
    'cccccccc-0000-0000-0000-000000000062',
    'cccccccc-0000-0000-0000-000000000063',
    'cccccccc-0000-0000-0000-000000000064',
    'cccccccc-0000-0000-0000-000000000065',
    'cccccccc-0000-0000-0000-000000000066',
    'cccccccc-0000-0000-0000-000000000067',
    'cccccccc-0000-0000-0000-000000000068',
    'cccccccc-0000-0000-0000-000000000069',
    'cccccccc-0000-0000-0000-000000000070',
    'cccccccc-0000-0000-0000-000000000071',
    'cccccccc-0000-0000-0000-000000000072',
    'cccccccc-0000-0000-0000-000000000073',
    'cccccccc-0000-0000-0000-000000000074',
    'cccccccc-0000-0000-0000-000000000075',
    'cccccccc-0000-0000-0000-000000000076',
    'cccccccc-0000-0000-0000-000000000077',
    'cccccccc-0000-0000-0000-000000000078',
    'cccccccc-0000-0000-0000-000000000079',
    'cccccccc-0000-0000-0000-000000000080',
    'cccccccc-0000-0000-0000-000000000081',
    'cccccccc-0000-0000-0000-000000000082',
    'cccccccc-0000-0000-0000-000000000083',
    'cccccccc-0000-0000-0000-000000000084',
    'cccccccc-0000-0000-0000-000000000085',
    'cccccccc-0000-0000-0000-000000000086',
    'cccccccc-0000-0000-0000-000000000087',
    'cccccccc-0000-0000-0000-000000000088',
    'cccccccc-0000-0000-0000-000000000089',
    'cccccccc-0000-0000-0000-000000000090',
    'cccccccc-0000-0000-0000-000000000091',
    'cccccccc-0000-0000-0000-000000000092',
    'cccccccc-0000-0000-0000-000000000093',
    'cccccccc-0000-0000-0000-000000000094',
];

// ── Zahlungsmethoden ─────────────────────────────────────────────────────────
// Entsprechen exakt dem PaymentMethod-Enum: CASH, CARD, ONLINE
export const PAYMENT_METHODS = ['CASH', 'CARD', 'ONLINE'];

// ── Rabattstufen ─────────────────────────────────────────────────────────────
export const DISCOUNT_RATES     = Array.from({ length: 46 }, (_, i) => 5 + i); // 5%–50%
export const LASTTEST_DISCOUNTS = [10, 30];

export const CURRENT_SCENARIO = __ENV.SCENARIO || 'lasttest';