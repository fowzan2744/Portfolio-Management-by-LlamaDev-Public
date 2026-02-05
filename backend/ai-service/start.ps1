Write-Host "Starting Gemini AI Service..." -ForegroundColor Cyan

if (Test-Path ".env") {
    Get-Content .env | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]*)\s*=\s*(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            Set-Item -Path "env:$name" -Value $value
            Write-Host "  Loaded $name" -ForegroundColor Gray
        }
    }
}

if (-not (Test-Path "venv")) {
    Write-Host "Creating virtual environment..." -ForegroundColor Yellow
    python -m venv venv
}

Write-Host "Activating virtual environment..." -ForegroundColor Gray
& ".\venv\Scripts\Activate.ps1"

if (-not (Test-Path "venv\Lib\site-packages\flask")) {
    Write-Host "Installing dependencies..." -ForegroundColor Yellow
    pip install -r requirements.txt --quiet
}

Write-Host ""
Write-Host "=== Gemini AI Service ===" -ForegroundColor Green
Write-Host "Status: Starting" -ForegroundColor Cyan
Write-Host "Port: 5000" -ForegroundColor Cyan
Write-Host "API Key: $(if($env:GEMINI_API_KEY){'Configured'}else{'NOT SET'})" -ForegroundColor $(if($env:GEMINI_API_KEY){'Green'}else{'Red'})
Write-Host ""
Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow
Write-Host ""

python gemini_service.py
