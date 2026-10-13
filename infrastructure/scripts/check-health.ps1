<#
.SYNOPSIS
Checks health of all Nexarats Academy local services and infrastructure.
#>

Write-Host "Checking Nexarats Academy Local Services..." -ForegroundColor Cyan

# 1. Check Docker Containers
if (Get-Command docker -ErrorAction SilentlyContinue) {
    Write-Host "`n[Docker Containers]" -ForegroundColor Yellow
    docker compose -f "$PSScriptRoot/../docker/docker-compose.yml" ps
} else {
    Write-Host "`nDocker is not installed or not in PATH." -ForegroundColor Red
}

$Port = if ($env:SERVER_PORT) { $env:SERVER_PORT } else { "8085" }

# 2. Check Backend Health Endpoint
Write-Host "`n[Backend Health (/api/v1/health on :$Port)]" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:$Port/api/v1/health" -Method Get -TimeoutSec 3
    Write-Host "Backend Status: " -NoNewline
    Write-Host $response.status -ForegroundColor Green
    $response | Format-List
} catch {
    Write-Host "Backend is not responding on http://localhost:$Port/api/v1/health ($($_.Exception.Message))" -ForegroundColor Red
}

# 3. Check Actuator Health
Write-Host "`n[Actuator Health (/actuator/health on :$Port)]" -ForegroundColor Yellow
try {
    $actuator = Invoke-RestMethod -Uri "http://localhost:$Port/actuator/health" -Method Get -TimeoutSec 3
    Write-Host "Actuator Status: " -NoNewline
    Write-Host $actuator.status -ForegroundColor Green
} catch {
    Write-Host "Actuator endpoint not reachable on :$Port ($($_.Exception.Message))" -ForegroundColor Red
}
