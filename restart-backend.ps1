# Restart Backend Script
Write-Host "🔄 Restarting RSU Registration Backend..." -ForegroundColor Cyan

# Find and kill existing Java process running the backend
Write-Host "Stopping existing backend process..." -ForegroundColor Yellow
Get-Process java -ErrorAction SilentlyContinue | Where-Object {
    $_.MainWindowTitle -match "rsu-registration-backend" -or
    $_.CommandLine -match "rsu-registration-backend-1.0.0.jar"
} | Stop-Process -Force

Start-Sleep -Seconds 2

# Navigate to backend directory
Set-Location "C:\Users\Jaycee\Desktop\RSU-FINAL-LABEXAM\rsu-registration-backend"

# Rebuild with latest changes
Write-Host "Rebuilding backend..." -ForegroundColor Yellow
mvn clean install -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Build successful!" -ForegroundColor Green
    
    # Start the backend
    Write-Host "🚀 Starting backend server on port 8080..." -ForegroundColor Green
    Start-Process -FilePath "java" -ArgumentList "-jar", "target\rsu-registration-backend-1.0.0.jar" -NoNewWindow
    
    Write-Host "✅ Backend started successfully!" -ForegroundColor Green
    Write-Host "Backend API: http://localhost:8080" -ForegroundColor Cyan
} else {
    Write-Host "❌ Build failed!" -ForegroundColor Red
}
