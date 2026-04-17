// ═══════════════════════════════════════════════════════════════════════════
// lib/auth.js – JWT-Token holen und Kassierer-Rotation pro VU
// ═══════════════════════════════════════════════════════════════════════════

import http from 'k6/http';
import { check } from 'k6';
import { fail } from 'k6';

import { ORCHESTRATOR_URL, ADMIN_USERNAME, ADMIN_PASSWORD, CASHIER_IDS } from './config.js';

/**
 * Holt einen JWT-Token vom Orchestrator.
 *
 * Wird in der setup()-Phase **einmalig** aufgerufen — alle VUs teilen sich
 * diesen Token über die Testdauer.
 *
 * Wenn ein Master-Token via ENV-Variable K6_MASTER_TOKEN gesetzt ist,
 * wird dieser verwendet (bypass Login). Praktisch für schnelle Debug-Runs.
 */
export function setupAuth() {
    // Option A: Master-Token via ENV-Variable (optional)
    if (__ENV.K6_MASTER_TOKEN) {
        console.log('[Auth] Verwende K6_MASTER_TOKEN aus Umgebungsvariable');
        return __ENV.K6_MASTER_TOKEN;
    }

    if (__ENV.K6_ENABLE_LEGACY_LOGIN !== 'true') {
        fail(
            '[Auth] Kein K6_MASTER_TOKEN gesetzt. ' +
            'Die Lasttests erwarten einen serverseitig erzeugten JWT. ' +
            'Setze K6_MASTER_TOKEN oder aktiviere K6_ENABLE_LEGACY_LOGIN=true fuer den alten /api/auth/token-Flow.'
        );
    }

    // Option B: Frisch einloggen
    console.log(`[Auth] Login als ${ADMIN_USERNAME} bei ${ORCHESTRATOR_URL}...`);

    const payload = JSON.stringify({
        username: ADMIN_USERNAME,
        password: ADMIN_PASSWORD,
    });

    const res = http.post(`${ORCHESTRATOR_URL}/api/auth/token`, payload, {
        headers: { 'Content-Type': 'application/json' },
        timeout: '15s',
    });

    const loginOk = check(res, {
        '[Auth] Login liefert 200': (r) => r.status === 200,
        '[Auth] Response enthält accessToken': (r) => r.json('accessToken') !== undefined,
    });

    if (!loginOk) {
        fail(`[Auth] Login fehlgeschlagen: status=${res.status} body=${res.body}`);
    }

    const token = res.json('accessToken');
    console.log(`[Auth] Token erhalten: ${token.substring(0, 40)}...`);
    return token;
}

/**
 * Baut Auth-Header für einen Request.
 */
export function authHeaders(token) {
    return {
        headers: {
            'Content-Type':  'application/json',
            'Accept':        'application/json',
            'Authorization': `Bearer ${token}`,
        },
        timeout: '10s',
    };
}

/**
 * Rotiert Kassierer-IDs basierend auf der VU-ID.
 * VU 1 → cashier.one (id=4), VU 2 → cashier.two (id=5), VU 3 → .three (id=6),
 * VU 4 → cashier.one wieder, usw.
 */
export function getCashierId(vuId) {
    return CASHIER_IDS[(vuId - 1) % CASHIER_IDS.length];
}
