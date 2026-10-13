<#
.SYNOPSIS
Starts Nexarats Academy local infrastructure services via Docker Compose.
#>

$ComposeFile = "$PSScriptRoot\..\docker\docker-compose.yml"

Write-Host "Starting Nexarats Academy Local Infrastructure (Postgres, Redis, MinIO)..." -ForegroundColor Cyan
docker compose -f $ComposeFile up -d

Write-Host "`nWaiting for database and redis readiness..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

docker compose -f $ComposeFile ps
Write-Host "`nInfrastructure started successfully." -ForegroundColor Green
Write-Host "PostgreSQL: localhost:5432 (User: nexarats_user, DB: nexarats_academy)"
Write-Host "Redis:      localhost:6379"
Write-Host "MinIO:      localhost:9000 (Console: localhost:9001)"
