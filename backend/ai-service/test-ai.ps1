# Test AI Service
Write-Host "Testing AI Service on http://localhost:5000" -ForegroundColor Cyan

# Test 1: Health check
Write-Host "`n[Test 1] Health check..." -ForegroundColor Yellow
try {
    $health = Invoke-WebRequest -Uri "http://localhost:5000/health" -UseBasicParsing
    $healthData = $health.Content | ConvertFrom-Json
    Write-Host "  Status: $($healthData.status)" -ForegroundColor Green
    Write-Host "  API Key: $($healthData.api_key_configured)" -ForegroundColor Green
} catch {
    Write-Host "  FAILED: $_" -ForegroundColor Red
    exit 1
}

# Test 2: AI Analysis
Write-Host "`n[Test 2] AI Analysis..." -ForegroundColor Yellow
$testBody = @{
    prompt = @"
Analyze this simple portfolio and return ONLY valid JSON:
Portfolio: 100 shares of AAPL valued at `$15,000

Return this exact JSON structure (keep it SHORT and SIMPLE):
{
  "summary": "Brief one sentence summary",
  "risks": ["Risk 1", "Risk 2"],
  "opportunities": ["Opportunity 1"],
  "rebalancingActions": [{"action": "HOLD", "symbol": "AAPL", "reason": "Stable position", "suggestedAllocation": 100.0}],
  "sentimentAnalysis": "Positive outlook",
  "nextSteps": ["Monitor performance"]
}
"@
} | ConvertTo-Json

try {
    Write-Host "  Sending request..." -ForegroundColor Gray
    $response = Invoke-WebRequest -Uri "http://localhost:5000/api/ai/analyze" `
        -Method POST `
        -Body $testBody `
        -ContentType "application/json" `
        -UseBasicParsing `
        -TimeoutSec 30
    
    Write-Host "  Status: $($response.StatusCode)" -ForegroundColor Green
    
    $content = $response.Content | ConvertFrom-Json
    Write-Host "  Response keys: $($content.PSObject.Properties.Name -join ', ')" -ForegroundColor Cyan
    Write-Host "`n  Summary: $($content.summary)" -ForegroundColor Yellow
    Write-Host "  Risks: $($content.risks.Count) items" -ForegroundColor Yellow
    Write-Host "  Sentiment: $($content.sentimentAnalysis)" -ForegroundColor Yellow
    
    Write-Host "`n[SUCCESS] All tests passed!" -ForegroundColor Green
    
} catch {
    Write-Host "  FAILED!" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        $reader.Close()
        Write-Host "`n  Server Response:" -ForegroundColor Yellow
        Write-Host "  $errorBody" -ForegroundColor Gray
    }
    exit 1
}
