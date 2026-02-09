import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://supplementhub.local";

export const options = {
    stages: [
        { duration: "10s", target: 5 },   // ramp up
        { duration: "20s", target: 15 },  // hold
        { duration: "10s", target: 0 },   // ramp down
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],        // <1% errors
        http_req_duration: ["p(95)<500"],      // 95% under 500ms
    },
};

export default function () {
    const res = http.get(`${BASE_URL}/gateway/actuator/health`);

    check(res, {
        "status 200": (r) => r.status === 200,
    });

    sleep(0.2);
}
