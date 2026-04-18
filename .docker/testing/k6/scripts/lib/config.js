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
// Werden in allen 5 Testarten gelegentlich direkt eingelöst (redeemVoucher).
// 5 verschiedene Codes damit nicht immer derselbe Voucher getroffen wird.
export const VOUCHER_REGULAR_CODES = [
    'bbbbbbbb-0000-0000-0000-000000000001',
    'bbbbbbbb-0000-0000-0000-000000000002',
    'bbbbbbbb-0000-0000-0000-000000000003',
    'bbbbbbbb-0000-0000-0000-000000000004',
    'bbbbbbbb-0000-0000-0000-000000000005',
];

// ── Zahlungsmethoden ─────────────────────────────────────────────────────────
// Entsprechen exakt dem PaymentMethod-Enum: CASH, CARD, ONLINE
export const PAYMENT_METHODS = ['CASH', 'CARD', 'ONLINE'];

// ── Rabattstufen ─────────────────────────────────────────────────────────────
export const DISCOUNT_RATES     = Array.from({ length: 46 }, (_, i) => 5 + i); // 5%–50%
export const LASTTEST_DISCOUNTS = [10, 30];

export const CURRENT_SCENARIO = __ENV.SCENARIO || 'lasttest';