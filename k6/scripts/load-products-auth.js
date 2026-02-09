import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://supplementhub.local";
const REALM = __ENV.KC_REALM || "supplementhub-microservices-security-realm";
const CLIENT_ID = __ENV.KC_CLIENT_ID || "k6-client";
const CLIENT_SECRET = __ENV.KC_CLIENT_SECRET || "wiZkL4lXpQNlmwPm4Rb4gjVMErCEsljG";
const PRODUCTS_PATH = __ENV.PRODUCTS_PATH || "/gateway/products";

const TOKEN_URL = `${BASE_URL}/keycloak/realms/${REALM}/protocol/openid-connect/token`;

export const options = {
    stages: [
        { duration: "10s", target: 5 },
        { duration: "20s", target: 10 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.02"],
        http_req_duration: ["p(95)<800"],
    },
};

function requireEnv(name, value) {
    if (!value) throw new Error(`Missing required env var: ${name}`);
}

// tiny form encoder that k6 actually supports
function formEncode(obj) {
    return Object.keys(obj)
        .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(obj[k])}`)
        .join("&");
}

export function setup() {
    requireEnv("KC_CLIENT_SECRET", CLIENT_SECRET);

    const body = formEncode({
        grant_type: "client_credentials",
        client_id: CLIENT_ID,
        client_secret: CLIENT_SECRET,
    });

    const res = http.post(TOKEN_URL, body, {
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        redirects: 0,
    });

    const ok = check(res, {
        "token endpoint is 200": (r) => r.status === 200,
        "token response is json": (r) =>
            (r.headers["Content-Type"] || "").toLowerCase().includes("application/json"),
    });

    if (!ok) {
        throw new Error(`Token request failed: status=${res.status} body=${res.body}`);
    }

    const token = res.json("access_token");
    if (!token) throw new Error(`No access_token in response: ${res.body}`);

    console.log(`Got token (first 30 chars): ${token.substring(0, 30)}...`);
    return { token };
}

export default function (data) {
    const res = http.get(`${BASE_URL}${PRODUCTS_PATH}`, {
        redirects: 0,
        headers: {
            Authorization: `Bearer ${data.token}`,
            Accept: "application/json",
        },
    });

    check(res, {
        "status is 200": (r) => r.status === 200,
        "not unauthorized": (r) => r.status !== 401,
        "not forbidden": (r) => r.status !== 403,
    });

    if (res.status !== 200 && (__ITER % 50 === 0)) {
        console.log(`Non-200: ${res.status} ${PRODUCTS_PATH}`);
    }

    sleep(0.3);
}
