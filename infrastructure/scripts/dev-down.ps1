<#
.SYNOPSIS
Stops Nexarats Academy local infrastructure services.
#>

$ComposeFile = "$PSScriptRoot\..\docker\docker-compose.yml"

Write-Host "Stopping Nexarats Academy Local Infrastructure..." -ForegroundColor Yellow
docker compose -f $ComposeFile down

Write-Host "Infrastructure stopped." -ForegroundColor Green
