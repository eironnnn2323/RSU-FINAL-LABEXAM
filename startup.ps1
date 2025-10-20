# ============================================================
# RSU Registration System - Startup Script
# Platform: Windows PowerShell
# Usage: .\startup.ps1
# ============================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "RSU Registration System - Startup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
Write-Host "[1/3] Checking Docker..." -ForegroundColor Yellow
try {
    docker ps > $null
    Write-Host "✓ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker is not running. Please start Docker Desktop." -ForegroundColor Red
    exit 1
}

# Start Docker services
Write-Host "[2/3] Starting RabbitMQ and PostgreSQL..." -ForegroundColor Yellow
cd docker
docker-compose up -d
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Docker services started" -ForegroundColor Green
} else {
    Write-Host "✗ Failed to start Docker services" -ForegroundColor Red
    exit 1
}

# Wait for services to be ready
Write-Host "[3/3] Waiting for services to be ready (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Verify services
$postgres = docker ps | Select-String "rsu_postgres"
$rabbitmq = docker ps | Select-String "rsu_rabbitmq"

if ($postgres -and $rabbitmq) {
    Write-Host "✓ All services are running" -ForegroundColor Green
} else {
    Write-Host "✗ Some services failed to start" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Services Ready!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Open a new PowerShell window and run:" -ForegroundColor White
Write-Host "   cd rsu-registration-backend" -ForegroundColor Cyan
Write-Host "   mvn spring-boot:run" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. Open another PowerShell window and run:" -ForegroundColor White
Write-Host "   cd rsu-registration-frontend" -ForegroundColor Cyan
Write-Host "   npm install" -ForegroundColor Cyan
Write-Host "   npm start" -ForegroundColor Cyan
Write-Host ""
Write-Host "3. Frontend will open automatically at http://localhost:3000" -ForegroundColor White
Write-Host ""
Write-Host "Useful Links:" -ForegroundColor Yellow
Write-Host "- Frontend: http://localhost:3000" -ForegroundColor Cyan
Write-Host "- Backend: http://localhost:8080" -ForegroundColor Cyan
Write-Host "- RabbitMQ: http://localhost:15672 (guest/guest)" -ForegroundColor Cyan
Write-Host ""
Write-Host "To stop all services, run: docker-compose down" -ForegroundColor Yellow
Write-Host ""
