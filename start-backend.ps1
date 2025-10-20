# START BACKEND SERVER

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Starting RSU Registration Backend" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to backend directory
Set-Location -Path "$PSScriptRoot\rsu-registration-backend"

Write-Host "Backend directory: $PWD" -ForegroundColor Yellow
Write-Host ""
Write-Host "Starting Spring Boot application..." -ForegroundColor Green
Write-Host "This will take about 10-15 seconds..." -ForegroundColor Yellow
Write-Host ""
Write-Host "Backend will be available at: http://localhost:8080" -ForegroundColor Green
Write-Host "RabbitMQ should be running (docker containers)" -ForegroundColor Green
Write-Host "PostgreSQL should be running (docker containers)" -ForegroundColor Green
Write-Host ""
Write-Host "IMPORTANT: Keep this window OPEN!" -ForegroundColor Red
Write-Host "Press Ctrl+C to stop the backend server" -ForegroundColor Red
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan

# Start Spring Boot with Maven
mvn spring-boot:run
