# 🚀 Trade Store Demo Guide

## 📋 Overview
This guide shows you how to run the TradeStore application and watch trades being inserted, updated, rejected, and automatically expired in real-time.

## 🔧 Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- curl (or any REST client)
- Terminal/Command Prompt

## 🚀 Step 1: Start the Application

### Option A: Main Application (Recommended)
```bash
# Open terminal in D:\Dev\TradeStore
mvn clean compile
mvn spring-boot:run -DskipTests
```

### Option B: Demo Application (Automated Demo)
```bash
# Set demo profile and run
set SPRING_PROFILES_ACTIVE=demo
mvn spring-boot:run -DskipTests
```

**Wait for:** You should see Spring Boot banner and "Started TradeStoreApplication" message.

## 📝 Step 2: Create Valid Trades (Should Succeed)

Open another terminal and run these commands:

### Trade 1 - Valid Future Trade
```bash
curl -X POST http://localhost:8080/api/trades \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "00000000-0000-0000-0000-000000000001",
    "version": 1,
    "counterPartyId": "COUNTER_PARTY_1",
    "bookId": "BOOK_1",
    "maturityDate": "2025-12-31",
    "createdDate": "2024-02-13",
    "expired": false
  }'
```
**Expected:** HTTP 201 Created + Trade object in response

### Trade 2 - Another Valid Trade
```bash
curl -X POST http://localhost:8080/api/trades \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "00000000-0000-0000-0000-000000000002",
    "version": 1,
    "counterPartyId": "COUNTER_PARTY_2",
    "bookId": "BOOK_2",
    "maturityDate": "2025-06-30",
    "createdDate": "2024-02-13",
    "expired": false
  }'
```
**Expected:** HTTP 201 Created

## ❌ Step 3: Create Invalid Trade (Should be Rejected)

### Trade with Past Maturity Date
```bash
curl -X POST http://localhost:8080/api/trades \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "00000000-0000-0000-0000-000000000003",
    "version": 1,
    "counterPartyId": "COUNTER_PARTY_3",
    "bookId": "BOOK_3",
    "maturityDate": "2023-01-01",
    "createdDate": "2024-02-13",
    "expired": false
  }'
```
**Expected:** HTTP 400 Bad Request + Error message about past maturity date

## 🔄 Step 4: Version Conflict Demo

### Update with Lower Version (Should be Rejected)
```bash
curl -X PUT http://localhost:8080/api/trades/00000000-0000-0000-0000-000000000001 \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "00000000-0000-0000-0000-000000000001",
    "version": 0,
    "counterPartyId": "COUNTER_PARTY_1",
    "bookId": "BOOK_1",
    "maturityDate": "2025-12-31",
    "createdDate": "2024-02-13",
    "expired": false
  }'
```
**Expected:** HTTP 400 Bad Request + Error about lower version

### Update with Higher Version (Should Succeed)
```bash
curl -X PUT http://localhost:8080/api/trades/00000000-0000-0000-0000-000000000001 \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "00000000-0000-0000-0000-000000000001",
    "version": 2,
    "counterPartyId": "COUNTER_PARTY_1",
    "bookId": "BOOK_1",
    "maturityDate": "2025-12-31",
    "createdDate": "2024-02-13",
    "expired": false
  }'
```
**Expected:** HTTP 200 OK + Updated trade with version 2

## ⏰ Step 5: Create Trades That Will Expire (For Expiry Demo)

### Create Trades Expiring Tomorrow
```bash
# Trade 4 - Will expire tomorrow
curl -X POST http://localhost:8080/api/trades \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "00000000-0000-0000-0000-000000000004",
    "version": 1,
    "counterPartyId": "COUNTER_PARTY_4",
    "bookId": "BOOK_4",
    "maturityDate": "2024-02-14",
    "createdDate": "2024-02-13",
    "expired": false
  }'

# Trade 5 - Will expire tomorrow
curl -X POST http://localhost:8080/api/trades \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "00000000-0000-0000-0000-000000000005",
    "version": 1,
    "counterPartyId": "COUNTER_PARTY_5",
    "bookId": "BOOK_5",
    "maturityDate": "2024-02-14",
    "createdDate": "2024-02-13",
    "expired": false
  }'
```

## 📊 Step 6: View All Trades

### Get All Trades
```bash
curl http://localhost:8080/api/trades | jq '.'
```
**Expected:** Array of all trades with their current status

### Get Specific Trade
```bash
curl http://localhost:8080/api/trades/00000000-0000-0000-0000-000000000001 | jq '.'
```

## ⏰ Step 7: Watch Automatic Expiry

### What to Watch For:
1. **Expiry Scheduler runs every minute** (check application logs)
2. **Trades 4 & 5 should automatically expire** after midnight
3. **Log messages showing expiry process**

### Monitor Expiry in Real-time:
```bash
# In the application terminal, watch for these messages:
# "Starting scheduled task to mark expired trades"
# "Found X trades to expire"
# "Marked trade TRADE_ID as expired"
# "Completed marking X trades as expired"
```

### Check if Trades Expired:
```bash
# Check Trade 4 status
curl http://localhost:8080/api/trades/00000000-0000-0000-0000-000000000004 | jq '.expired'

# Check Trade 5 status  
curl http://localhost:8080/api/trades/00000000-0000-0000-0000-000000000005 | jq '.expired'
```

## 🔍 Step 8: Advanced Features (Optional)

### Use Optimized Endpoints (if available)
```bash
# Paginated trades
curl "http://localhost:8080/api/v2/trades?page=0&size=10&sortBy=createdDate&sortDir=desc"

# Trade statistics
curl http://localhost:8080/api/v2/trades/stats

# Bulk trade creation
curl -X POST http://localhost:8080/api/v2/trades/bulk \
  -H "Content-Type: application/json" \
  -d '[{"tradeId": "...", "version": 1, ...}]'
```

### H2 Database Console (for debugging)
Open: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (blank)

## 📋 Expected Results Summary

| **Operation** | **Expected Result** | **What to Watch** |
|---------------|-------------------|-------------------|
| **Valid Trade Creation** | HTTP 201 + Trade object | Application logs: "Processing trade..." |
| **Invalid Trade (Past Date)** | HTTP 400 + Error | Logs: "Validation failed" |
| **Lower Version Update** | HTTP 400 + Error | Logs: "Version validation failed" |
| **Higher Version Update** | HTTP 200 + Updated trade | Logs: "Replacing existing trade" |
| **Expiry Process** | Trades marked expired=true | Logs: "Starting scheduled task" |
| **Database Operations** | Data persistence | H2 console or logs |

## 🐛 Troubleshooting

### Application Won't Start
```bash
# Check for port conflicts
netstat -ano | findstr :8080

# Clean and rebuild
mvn clean compile
```

### Connection Refused
```bash
# Verify application is running
curl http://localhost:8080/actuator/health

# Check logs for errors
```

### Trades Not Expiring
- **Check system date** - trades expire only after maturity date
- **Wait for scheduler** - runs every minute at :00 seconds
- **Check logs** - should show expiry process

### PowerShell Users
Use `trade-demo.ps1` instead of bash commands:
```powershell
.\trade-demo.ps1
```

## 🎯 Key Learning Points

1. **✅ Validation**: Trades with past dates are rejected
2. **✅ Version Control**: Lower versions are rejected, higher versions accepted
3. **✅ Persistence**: All valid trades are stored in H2 database
4. **✅ Automatic Expiry**: Scheduler runs every minute to expire matured trades
5. **✅ Error Handling**: Proper HTTP status codes and error messages
6. **✅ Logging**: Detailed logs for monitoring and debugging

## 📊 Performance Monitoring

Watch the application logs for:
- Trade processing time
- Database operation efficiency
- Memory usage patterns
- Expiry scheduler performance

## 🎉 Demo Complete!

You've successfully witnessed:
- ✅ Trade insertion and validation
- ✅ Rejection of invalid trades
- ✅ Version conflict handling
- ✅ Real-time automatic expiry
- ✅ Comprehensive logging and monitoring

The TradeStore application is working as designed with proper validation, persistence, and automated expiry management!
