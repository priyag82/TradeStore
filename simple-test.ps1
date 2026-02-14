# Simple Trade Store Test
Write-Host "🚀 Testing Trade Store Application" -ForegroundColor Blue

$baseUrl = "http://localhost:8080/api/trades"

# Test 1: Create valid trade
try {
    $trade = @{
        tradeId = "00000000-0000-0000-0000-000000000001"
        version = 1
        counterPartyId = "COUNTER_PARTY_1"
        bookId = "BOOK_1"
        maturityDate = "2025-12-31"
        createdDate = "2024-02-13"
        expired = $false
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl" -Method Post -Body $trade -ContentType "application/json"
    Write-Host "✅ Trade created successfully! ID: $($response.tradeId)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Get all trades
try {
    $trades = Invoke-RestMethod -Uri "$baseUrl" -Method Get
    Write-Host "📊 Total trades: $($trades.Count)" -ForegroundColor Blue
    foreach ($t in $trades) {
        Write-Host "  - $($t.tradeId) v$($t.version) - $($t.counterPartyId)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Error getting trades: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "🎉 Test completed!" -ForegroundColor Cyan
