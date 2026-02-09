# k6 Load Tests (SupplementHub)

## Prereqs
- k6 installed (`k6 version`)
- Minikube running + ingress available
- Hosts file entry: `127.0.0.1 supplementhub.local`
- `minikube tunnel` running in a separate terminal

## Run smoke test
k6 run .\k6\scripts\smoke-health.js

## Run load test
k6 run .\k6\scripts\load-gateway-health.js

## Override base URL
k6 run -e BASE_URL=http://supplementhub.local .\k6\scripts\load-gateway-health.js

## Save JSON output
k6 run --out json=.\k6\results\gateway-health.json .\k6\scripts\load-gateway-health.js
