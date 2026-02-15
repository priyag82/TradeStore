# Trade Store API Test Script
# This script can be used to test the Trade Store API endpoints

# Base URL
$baseUrl = "http://localhost:8080/api/trades"

# Test 1: Get all trades (should return empty list initially)
Write-Host "Test 1: GET all trades"
try {
    $response = Invoke-RestMethod -Uri $baseUrl -Method GET -Headers @{"Accept"="application/json"}
    Write-Host "✓ GET all trades successful"
    Write-Host "Response: $response"
} catch {
    Write-Host "✗ GET all trades failed: $($_.Exception.Message)"
}

# Test 2: Create a new trade
Write-Host "`nTest 2: POST create trade"
$tradeData = @{
    tradeId = "550e8400-e29b-41d4-a716-446655440000"
    version = 1
    counterPartyId = "CP-1"
    bookId = "B1"
    maturityDate = "2025-12-31"
    createdDate = "2025-02-15"
    expired = $false
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri $baseUrl -Method POST -Body $tradeData -Headers @{
        "Content-Type"="application/json"
        "Accept"="application/json"
    }
    Write-Host "✓ POST create trade successful"
    Write-Host "Response: $response"
} catch {
    Write-Host "✗ POST create trade failed: $($_.Exception.Message)"
}

# Test 3: Get specific trade
Write-Host "`nTest 3: GET specific trade"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/550e8400-e29b-41d4-a716-446655440000" -Method GET -Headers @{"Accept"="application/json"}
    Write-Host "✓ GET specific trade successful"
    Write-Host "Response: $response"
} catch {
    Write-Host "✗ GET specific trade failed: $($_.Exception.Message)"
}

# Test 4: Update trade (higher version)
Write-Host "`nTest 4: PUT update trade"
$updatedTradeData = @{
    tradeId = "550e8400-e29b-41d4-a716-446655440000"
    version = 2
    counterPartyId = "CP-1"
    bookId = "B1"
    maturityDate = "2025-12-31"
    createdDate = "2025-02-15"
    expired = $false
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/550e8400-e29b-41d4-a716-446655440000" -Method PUT -Body $updatedTradeData -Headers @{
        "Content-Type"="application/json"
        "Accept"="application/json"
    }
    Write-Host "✓ PUT update trade successful"
    Write-Host "Response: $response"
} catch {
    Write-Host "✗ PUT update trade failed: $($_.Exception.Message)"
}

Write-Host "`nAPI Test Script Completed"
