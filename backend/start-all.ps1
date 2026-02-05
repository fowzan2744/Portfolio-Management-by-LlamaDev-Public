# Combined startup script for Portfolio Backend
Write-Host "=== Portfolio Backend Startup ===" -ForegroundColor Cyan
Write-Host ""

# Start Python AI Service in new window
Write-Host "1. Starting Python AI Service..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'C:\Users\Administrator\Desktop\New folder\Portfolio-Management-by-LlamaDev-Public\backend\ai-service'; .\start.ps1"

# Wait for Python service to start
Write-Host "   Waiting for AI service to initialize..." -ForegroundColor Gray
Start-Sleep -Seconds 8

# Test Python service
try {
    $health = Invoke-WebRequest -Uri "http://localhost:5000/health" -UseBasicParsing -TimeoutSec 5
    Write-Host "    Python AI Service is ready" -ForegroundColor Green
} catch {
    Write-Host "    Python AI Service not responding" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "2. Starting Java Backend..." -ForegroundColor Yellow
Write-Host "   Backend will start on port 8080" -ForegroundColor Gray
Write-Host ""

# Start Java backend
cd "C:\Users\Administrator\Desktop\New folder\Portfolio-Management-by-LlamaDev-Public\backend"
java -jar target\Portfolio-Backend-0.0.1-SNAPSHOT.jar
