import http from 'k6/http';
import { check, fail } from 'k6';

export function setupAuth() {
    if (__ENV.K6_MASTER_TOKEN) {
        return __ENV.K6_MASTER_TOKEN;
    }

    const url = `${__ENV.KEYCLOAK_URL}/realms/${__ENV.KEYCLOAK_REALM}/protocol/openid-connect/token`;

    const payload = {
        grant_type: 'password',
        client_id: __ENV.KEYCLOAK_CLIENT_ID,
        client_secret: __ENV.KEYCLOAK_CLIENT_SECRET,
        username: __ENV.KEYCLOAK_USERNAME,
        password: __ENV.KEYCLOAK_PASSWORD,
    };

    const res = http.post(url, payload, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        timeout: '15s',
    });

    const loginOk = check(res, {
        'Keycloak status is 200': (r) => r.status === 200,
        'Keycloak response contains access_token': (r) => r.json('access_token') !== undefined,
    });

    if (!loginOk) {
        fail(`Keycloak authentication failed: status=${res.status} body=${res.body}`);
    }

    return res.json('access_token');
}

/**
 * Generates authorization headers with explicit API Gateway routing identifiers.
 */
export function authHeaders(token, storeId = 1, registerId = 1) {
    return {
        headers: {
            'Content-Type':  'application/json',
            'Accept':        'application/json',
            'Authorization': `Bearer ${token}`,
            'X-Store-ID':    String(storeId),
            'X-Register-ID': String(registerId),
        },
        timeout: '10s',
    };
}