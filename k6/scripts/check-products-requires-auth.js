import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://supplementhub.local";
const PATH = __ENV.PRODUCTS_PATH || "/gateway/products";

export const options = {
    vus: 5,
    duration: "10s",
    thresholds: {
        http_req_duration: ["p(95)<800"],
        // Only count failures for requests where we *expected* success.
        "http_req_failed{expected_success:true}": ["rate<0.05"],
    },
};

export default function () {
    const res = http.get(`${BASE_URL}${PATH}`, {
        redirects: 0,
        tags: { expected_success: "false" }, // this request expects 401
    });

    check(res, {
        "products endpoint is protected (401)": (r) => r.status === 401,
        // header name sometimes varies; include basic check:
        "server asks for bearer token": (r) =>
            String(r.headers["WWW-Authenticate"] || "").toLowerCase().includes("bearer"),
    });

    sleep(0.3);
}
