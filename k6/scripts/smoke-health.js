import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://supplementhub.local";

export const options = {
    vus: 1,
    duration: "10s",
};

export default function () {
    const res = http.get(`${BASE_URL}/gateway/actuator/health`);

    check(res, {
        "gateway health is 200": (r) => r.status === 200,
        "gateway health is UP": (r) => r.body && r.body.includes('"status":"UP"'),
    });

    sleep(1);
}
