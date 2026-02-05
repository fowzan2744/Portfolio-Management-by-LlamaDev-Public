# Portfolio Backend - Environment Setup

## Gemini API Key Configuration

The backend automatically loads the `GEMINI_API_KEY` from multiple sources in this order:

1. **System Environment Variable** (highest priority)
2. **.env file** in the backend directory
3. **application.properties** fallback value

### Option 1: System Environment Variable (Recommended for Production)

**Windows PowerShell:**
```powershell
# Set for current user (persists across sessions)
[System.Environment]::SetEnvironmentVariable('GEMINI_API_KEY', 'your-api-key-here', 'User')

# Verify it's set
$env:GEMINI_API_KEY = [System.Environment]::GetEnvironmentVariable('GEMINI_API_KEY', 'User')
Write-Host "GEMINI_API_KEY: $env:GEMINI_API_KEY"
```

### Option 2: .env File (Recommended for Development)

1. The `.env` file has been created in the backend directory
2. Edit the file and update your API key if needed
3. The `start-backend.ps1` script will automatically load it

### Starting the Backend

Simply run:
```powershell
cd backend
.\start-backend.ps1
```

The script will:
- Load environment variables from `.env` if it exists
- Fall back to system environment variables
- Start the backend with the API key configured

### Rebuilding

If you make code changes:
```powershell
cd backend
mvn clean package -DskipTests
.\start-backend.ps1
```
