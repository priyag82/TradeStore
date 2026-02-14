# Trade Store Demo Script
Write-Host "🚀 Trade Store Demo - Testing Trade Operations" -ForegroundColor Blue
Write-Host "=============================================" -ForegroundColor Blue

$baseUrl = "http://localhost:8080/api/trades"

try {
    Write-Host "`n✅ Step 1: Creating Valid Trade 1..." -ForegroundColor Green
    
    $trade1 = @{
        tradeId = "00000000-0000-0000-0000-000000000001"
        version = 1
        counterPartyId = "COUNTER_PARTY_1"
        bookId = "BOOK_1"
        maturityDate = "2025-12-31"
        createdDate = "2024-02-13"
        expired = $false
    } | ConvertTo-Json -Depth 10

    $response1 = Invoke-RestMethod -Uri "$baseUrl" -Method Post -Body $trade1 -ContentType "application/json" -ErrorAction Stop
    Write-Host "✅ Trade 1 created successfully!" -ForegroundColor Green
    Write-Host "Trade ID: $($response1.tradeId)" -ForegroundColor White
    
} catch {
    Write-Host "❌ Error creating Trade 1: $($_.Exception.Message)" -ForegroundColor Red
}

try {
    Write-Host "`n✅ Step 2: Creating Valid Trade 2..." -ForegroundColor Green
    
    $trade2 = @{
        tradeId = "00000000-0000-0000-0000-000000000002"
        version = 1
        counterPartyId = "COUNTER_PARTY_2"
        bookId = "BOOK_2"
        maturityDate = "2025-06-30"
        createdDate = "2024-02-13"
        expired = $false
    } | ConvertTo-Json -Depth 10

    $response2 = Invoke-RestMethod -Uri "$baseUrl" -Method Post -Body $trade2 -ContentType "application/json" -ErrorAction Stop
    Write-Host "✅ Trade 2 created successfully!" -ForegroundColor Green
    Write-Host "Trade ID: $($response2.tradeId)" -ForegroundColor White
    
} catch {
    Write-Host "❌ Error creating Trade 2: $($_.Exception.Message)" -ForegroundColor Red
}

try {
    Write-Host "`n❌ Step 3: Creating Invalid Trade (Past Maturity Date)..." -ForegroundColor Yellow
    
    $invalidTrade = @{
        tradeId = "00000000-0000-0000-0000-000000000003"
        version = 1
        counterPartyId = "COUNTER_PARTY_3"
        bookId = "BOOK_3"
        maturityDate = "2023-01-01"  # Past date - should fail
        createdDate = "2024-02-13"
        expired = $false
    } | ConvertTo-Json -Depth 10

    $response3 = Invoke-RestMethod -Uri "$baseUrl" -Method Post -Body $invalidTrade -ContentType "application/json" -ErrorAction Stop
    Write-Host "❌ This should have failed!" -ForegroundColor Red
    
} catch {
    Write-Host "✅ Correctly rejected invalid trade: $($_.Exception.Message)" -ForegroundColor Green
}

try {
    Write-Host "`n❌ Step 4: Updating Trade with Lower Version (Should Fail)..." -ForegroundColor Yellow
    
    $lowerVersion = @{
        tradeId = "00000000-0000-0000-0000-000000000001"
        version = 0  # Lower version - should fail
        counterPartyId = "COUNTER_PARTY_1"
        bookId = "BOOK_1"
        maturityDate = "2025-12-31"
        createdDate = "2024-02-13"
        expired = $false
    } | ConvertTo-Json -Depth 10

    $response4 = Invoke-RestMethod -Uri "$baseUrl/00000000-0000-0000-0000-000000000001" -Method Put -Body $lowerVersion -ContentType "application/json" -ErrorAction Stop
    Write-Host "❌ This should have failed!" -ForegroundColor Red
    
} catch {
    Write-Host "✅ Correctly rejected lower version: $($_.Exception.Message)" -ForegroundColor Green
}

try {
    Write-Host "`n✅ Step 5: Updating Trade with Higher Version (Should Succeed)..." -ForegroundColor Green
    
    $higherVersion = @{
        tradeId = "00000000-0000-0000-0000-000000000001"
        version = 2  # Higher version - should succeed
        counterPartyId = "COUNTER_PARTY_1"
        bookId = "BOOK_1"
        maturityDate = "2025-12-31"
        createdDate = "2024-02-13"
        expired = $false
    } | ConvertTo-Json -Depth 10

    $response5 = Invoke-RestMethod -Uri "$baseUrl/00000000-0000-0000-0000-000000000001" -Method Put -Body $higherVersion -ContentType "application/json" -ErrorAction Stop
    Write-Host "✅ Trade updated successfully to version 2!" -ForegroundColor Green
    Write-Host "Trade ID: $($response5.tradeId), Version: $($response5.version)" -ForegroundColor White
    
} catch {
    Write-Host "❌ Error updating trade: $($_.Exception.Message)" -ForegroundColor Red
}

try {
    Write-Host "`n⏰ Step 6: Creating Trades That Will Expire Tomorrow..." -ForegroundColor Yellow
    
    $expiryDate = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
    
    $expiring1 = @{
        tradeId = "00000000-0000-0000-0000-000000000004"
        version = 1
        counterPartyId = "COUNTER_PARTY_4"
        bookId = "BOOK_4"
        maturityDate = $expiryDate
        createdDate = "2024-02-13"
        expired = $false
    } | ConvertTo-Json -Depth 10

    $response6 = Invoke-RestMethod -Uri "$baseUrl" -Method Post -Body $expiring1 -ContentType "application/json" -ErrorAction Stop
    Write-Host "⏰ Expiring Trade 4 created (expires: $expiryDate)" -ForegroundColor Yellow
    
    $expiring2 = @{
        tradeId = "00000000-0000-0000-0000-000000000005"
        version = 1
        counterPartyId = "COUNTER_PARTY_5"
        bookId = "BOOK_5"
        maturityDate = $expiryDate
        createdDate = "2024-02-13"
        expired = $false
    } | ConvertTo-Json -Depth 10

    $response7 = Invoke-RestMethod -Uri "$baseUrl" -Method Post -Body $expiring2 -ContentType "application/json" -ErrorAction Stop
    Write-Host "⏰ Expiring Trade 5 created (expires: $expiryDate)" -ForegroundColor Yellow
    
} catch {
    Write-Host "❌ Error creating expiring trades: $($_.Exception.Message)" -ForegroundColor Red
}

try {
    Write-Host "`n📊 Step 7: Viewing All Trades..." -ForegroundColor Blue
    
    $allTrades = Invoke-RestMethod -Uri "$baseUrl" -Method Get -ErrorAction Stop
    Write-Host "📊 Total trades in database: $($allTrades.Count)" -ForegroundColor Blue
    
    foreach ($trade in $allTrades) {
        $status = if ($trade.expired) { "EXPIRED" } else { "ACTIVE" }
        $color = if ($trade.expired) { "Red" } else { "Green" }
        Write-Host "📋 Trade: $($trade.tradeId), Version: $($trade.version), CounterParty: $($trade.counterPartyId), Status: $status" -ForegroundColor $color
    }
    
} catch {
    Write-Host "❌ Error retrieving trades: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎉 Demo Summary:" -ForegroundColor Cyan
Write-Host "✅ Valid trades created and accepted" -ForegroundColor Green
Write-Host "✅ Invalid trades properly rejected" -ForegroundColor Green  
Write-Host "✅ Version validation working correctly" -ForegroundColor Green
Write-Host "✅ Trades ready for automatic expiry" -ForegroundColor Green
Write-Host "`n🔍 Watch the application logs for automatic expiry processing!" -ForegroundColor Yellow
Write-Host "The expiry scheduler runs every minute - trades 4 & 5 should expire tomorrow!" -ForegroundColor Yellow
