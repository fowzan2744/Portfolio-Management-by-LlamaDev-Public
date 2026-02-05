# Start Portfolio Backend with Gemini API Key
# Make sure to rebuild first: mvn clean package -DskipTests

# Navigate to backend directory
Set-Location $PSScriptRoot

# Load environment variables from .env file if it exists
$envFile = Join-Path $PSScriptRoot ".env"
$envVars = @{}

if (Test-Path $envFile) {
    Write-Host "Loading environment variables from .env file..." -ForegroundColor Cyan
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]*)\s*=\s*(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            $envVars[$name] = $value
            Write-Host "  Loaded $name" -ForegroundColor Gray
        }
    }
}

# Set GEMINI_API_KEY for this process
if ($envVars.ContainsKey('GEMINI_API_KEY')) {
    $env:GEMINI_API_KEY = $envVars['GEMINI_API_KEY']
}

# Check if GEMINI_API_KEY is set (from .env, system env, or user env)
if (-not $env:GEMINI_API_KEY) {
    # Try to get from User environment variable
    $env:GEMINI_API_KEY = [System.Environment]::GetEnvironmentVariable('GEMINI_API_KEY', 'User')
}

if ($env:GEMINI_API_KEY) {
    Write-Host "GEMINI_API_KEY is configured (length: $($env:GEMINI_API_KEY.Length))" -ForegroundColor Green
} else {
    Write-Host "WARNING: GEMINI_API_KEY is not set!" -ForegroundColor Yellow
}

# Check if JAR file exists
$jarFile = Join-Path $PSScriptRoot "target\Portfolio-Backend-0.0.1-SNAPSHOT.jar"
if (-not (Test-Path $jarFile)) {
    Write-Host "ERROR: Backend JAR file not found!" -ForegroundColor Red
    Write-Host "Please build first: mvn clean package -DskipTests" -ForegroundColor Yellow
    exit 1
}

# Check if Python AI service is running (optional but recommended)
try {
    $pythonHealth = Invoke-WebRequest -Uri "http://localhost:5000/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
    $healthData = $pythonHealth.Content | ConvertFrom-Json
    if ($healthData.api_key_configured -eq $true) {
        Write-Host "[OK] Python AI Service detected and configured" -ForegroundColor Green
    } else {
        Write-Host "[WARN] Python AI Service is running but API key not configured" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[WARN] Python AI Service not detected on port 5000" -ForegroundColor Yellow
    Write-Host "  AI features will use fallback mode" -ForegroundColor Gray
    Write-Host "  To enable AI: start Python service first" -ForegroundColor Gray
}

# Start the backend
Write-Host ""
Write-Host "Starting Portfolio Backend on port 8080..." -ForegroundColor Green
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Yellow
Write-Host ""

java -jar target/Portfolio-Backend-0.0.1-SNAPSHOT.jar
