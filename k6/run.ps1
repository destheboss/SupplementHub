param(
  [string]$BaseUrl = "http://supplementhub.local"
)

Write-Host "Running k6 smoke test..." -ForegroundColor Cyan
k6 run -e BASE_URL=$BaseUrl .\k6\scripts\smoke-health.js

Write-Host ""
Write-Host "Running k6 load test..." -ForegroundColor Cyan
k6 run -e BASE_URL=$BaseUrl .\k6\scripts\load-gateway-health.js

Write-Host ""
Write-Host "Running k6 products load test..." -ForegroundColor Cyan
k6 run -e BASE_URL=$BaseUrl .\k6\scripts\check-products-requires-auth.js