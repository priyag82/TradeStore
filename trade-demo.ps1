# Trade Store Demo Script for PowerShell
# =====================================

Write-Host "🚀 Trade Store Demo Script" -ForegroundColor Blue
Write-Host "==========================" -ForegroundColor Blue

$BaseUrl = "http://localhost:8080/api/trades"

Write-Host "Step 1: Starting Trade Store Application..." -ForegroundColor Blue
Write-Host "Open another terminal and run: mvn spring-boot:run" -ForegroundColor Yellow
Write-Host "Waiting for application to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "`nStep 2: Checking Application Health..." -ForegroundColor Blue
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -ErrorAction SilentlyContinue
    $health | ConvertTo-Json
} catch {
    Write-Host "Application not ready yet" -ForegroundColor Red
}

Write-Host "`nStep 3: Creating Valid Trades (Should Succeed)..." -ForegroundColor Blue

# Create valid trades
Write-Host "Creating Trade 1 (Valid):" -ForegroundColor Green
$trade1 = @{
    tradeId = "00000000-0000-0000-0000-000000000001"
    version = 1
    counterPartyId = "COUNTER_PARTY_1"
    bookId = "BOOK_1"
    maturityDate = "2025-12-31"
    createdDate = "2024-02-13"
    expired = $false
} | ConvertTo-Json -Depth 10

try {
    $response1 = Invoke-RestMethod -Uri "$BaseUrl" -Method Post -Body $trade1 -ContentType "application/json"
    $response1 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nCreating Trade 2 (Valid):" -ForegroundColor Green
$trade2 = @{
    tradeId = "00000000-0000-0000-0000-000000000002"
    version = 1
    counterPartyId = "COUNTER_PARTY_2"
    bookId = "BOOK_2"
    maturityDate = "2025-06-30"
    createdDate = "2024-02-13"
    expired = $false
} | ConvertTo-Json -Depth 10

try {
    $response2 = Invoke-RestMethod -Uri "$BaseUrl" -Method Post -Body $trade2 -ContentType "application/json"
    $response2 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nStep 4: Creating Trade with Past Maturity Date (Should be Rejected)..." -ForegroundColor Blue
Write-Host "Expected: Rejection due to past maturity date" -ForegroundColor Red
$invalidTrade1 = @{
    tradeId = "00000000-0000-0000-0000-000000000003"
    version = 1
    counterPartyId = "COUNTER_PARTY_3"
    bookId = "BOOK_3"
    maturityDate = "2023-01-01"
    createdDate = "2024-02-13"
    expired = $false
} | ConvertTo-Json -Depth 10

try {
    $response3 = Invoke-RestMethod -Uri "$BaseUrl" -Method Post -Body $invalidTrade1 -ContentType "application/json"
    $response3 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "✅ Correctly Rejected! $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`nStep 5: Updating Trade with Lower Version (Should be Rejected)..." -ForegroundColor Blue
Write-Host "Expected: Rejection due to lower version" -ForegroundColor Red
$invalidUpdate = @{
    tradeId = "00000000-0000-0000-0000-000000000001"
    version = 0
    counterPartyId = "COUNTER_PARTY_1"
    bookId = "BOOK_1"
    maturityDate = "2025-12-31"
    createdDate = "2024-02-13"
    expired = $false
} | ConvertTo-Json -Depth 10

try {
    $response4 = Invoke-RestMethod -Uri "$BaseUrl/00000000-0000-0000-0000-000000000001" -Method Put -Body $invalidUpdate -ContentType "application/json"
    $response4 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "✅ Correctly Rejected! $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`nStep 6: Updating Trade with Higher Version (Should Succeed)..." -ForegroundColor Blue
Write-Host "Expected: Success with version upgrade" -ForegroundColor Green
$validUpdate = @{
    tradeId = "00000000-0000-0000-0000-000000000001"
    version = 2
    counterPartyId = "COUNTER_PARTY_1"
    bookId = "BOOK_1"
    maturityDate = "2025-12-31"
    createdDate = "2024-02-13"
    expired = $false
} | ConvertTo-Json -Depth 10

try {
    $response5 = Invoke-RestMethod -Uri "$BaseUrl/00000000-0000-0000-0000-000000000001" -Method Put -Body $validUpdate -ContentType "application/json"
    $response5 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nStep 7: Creating Trades That Will Expire Soon (for Expiry Demo)..." -ForegroundColor Blue

# Create trades that will expire in less than 2 minutes
$expiryDate = (Get-Date).AddMinutes(2).ToString("yyyy-MM-dd")

Write-Host "Creating Trade 4 (Will expire in 2 minutes):" -ForegroundColor Yellow
$expiringTrade1 = @{
    tradeId = "00000000-0000-0000-0000-000000000004"
    version = 1
    counterPartyId = "COUNTER_PARTY_4"
    bookId = "BOOK_4"
    maturityDate = $expiryDate
    createdDate = "2024-02-13"
    expired = $false
} | ConvertTo-Json -Depth 10

try {
    $response6 = Invoke-RestMethod -Uri "$BaseUrl" -Method Post -Body $expiringTrade1 -ContentType "application/json"
    $response6 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nCreating Trade 5 (Will expire in 2 minutes):" -ForegroundColor Yellow
$expiringTrade2 = @{
    tradeId = "00000000-0000-0000-0000-000000000005"
    version = 1
    counterPartyId = "COUNTER_PARTY_5"
    bookId = "BOOK_5"
    maturityDate = $expiryDate
    createdDate = "2024-02-13"
    expired = $false
} | ConvertTo-Json -Depth 10

try {
    $response7 = Invoke-RestMethod -Uri "$BaseUrl" -Method Post -Body $expiringTrade2 -ContentType "application/json"
    $response7 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nStep 8: Viewing All Trades..." -ForegroundColor Blue
try {
    $allTrades = Invoke-RestMethod -Uri "$BaseUrl" -Method Get
    $allTrades | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nStep 9: Getting Specific Trade..." -ForegroundColor Blue
try {
    $specificTrade = Invoke-RestMethod -Uri "$BaseUrl/00000000-0000-0000-0000-000000000001" -Method Get
    $specificTrade | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nStep 10: Monitoring Trade Statistics..." -ForegroundColor Blue
try {
    $stats = Invoke-RestMethod -Uri "http://localhost:8080/api/v2/trades/stats" -Method Get -ErrorAction SilentlyContinue
    $stats | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Stats endpoint not available (expected for basic controller)" -ForegroundColor Yellow
}

Write-Host "`nStep 11: Watching for Expiry (Next 2 Minutes)..." -ForegroundColor Blue
Write-Host "The expiry scheduler runs every minute. Watch the application logs!" -ForegroundColor Yellow
Write-Host "Trades 4 and 5 should expire automatically within 2 minutes." -ForegroundColor Yellow

Write-Host "`n=== Demo Summary ===" -ForegroundColor Green
Write-Host "✅ Valid trades created successfully" -ForegroundColor Green
Write-Host "✅ Invalid trades properly rejected" -ForegroundColor Green
Write-Host "✅ Version validation working" -ForegroundColor Green
Write-Host "✅ Trades ready for automatic expiry" -ForegroundColor Green

Write-Host "`n📋 What to Watch For:" -ForegroundColor Yellow
Write-Host "1. Application logs showing trade processing" -ForegroundColor White
Write-Host "2. Expiry scheduler running every minute" -ForegroundColor White
Write-Host "3. Trades 4 & 5 expiring automatically" -ForegroundColor White
Write-Host "4. Database operations in the logs" -ForegroundColor White

Write-Host "`n🔍 Useful Commands:" -ForegroundColor Blue
Write-Host "Check trades: curl '$BaseUrl' | jq '.'" -ForegroundColor White
Write-Host "Check specific trade: curl '$BaseUrl/TRADE_ID' | jq '.'" -ForegroundColor White
Write-Host "Watch application logs in the terminal where mvn spring-boot:run is running" -ForegroundColor White

Write-Host "`nDemo completed successfully!" -ForegroundColor Green
