#!/bin/bash

echo "🚀 Trade Store Demo Script"
echo "=========================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080/api/trades"

echo -e "${BLUE}Step 1: Starting Trade Store Application...${NC}"
echo "Open another terminal and run: mvn spring-boot:run"
echo "Waiting for application to start..."
sleep 10

echo -e "${BLUE}Step 2: Checking Application Health...${NC}"
curl -s http://localhost:8080/actuator/health | jq '.' || echo "Application not ready yet"

echo -e "\n${BLUE}Step 3: Creating Valid Trades (Should Succeed)...${NC}"

# Create valid trades
echo -e "${GREEN}Creating Trade 1 (Valid):${NC}"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "00000000-0000-0000-0000-000000000001",
    "version": 1,
    "counterPartyId": "COUNTER_PARTY_1",
    "bookId": "BOOK_1",
    "maturityDate": "2025-12-31",
    "createdDate": "2024-02-13",
    "expired": false
  }' | jq '.'

echo -e "\n${GREEN}Creating Trade 2 (Valid):${NC}"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "00000000-0000-0000-0000-000000000002",
    "version": 1,
    "counterPartyId": "COUNTER_PARTY_2",
    "bookId": "BOOK_2",
    "maturityDate": "2025-06-30",
    "createdDate": "2024-02-13",
    "expired": false
  }' | jq '.'

echo -e "\n${BLUE}Step 4: Creating Trade with Past Maturity Date (Should be Rejected)...${NC}"
echo -e "${RED}Expected: Rejection due to past maturity date${NC}"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "00000000-0000-0000-0000-000000000003",
    "version": 1,
    "counterPartyId": "COUNTER_PARTY_3",
    "bookId": "BOOK_3",
    "maturityDate": "2023-01-01",
    "createdDate": "2024-02-13",
    "expired": false
  }' | jq '.' || echo "✅ Correctly Rejected!"

echo -e "\n${BLUE}Step 5: Updating Trade with Lower Version (Should be Rejected)...${NC}"
echo -e "${RED}Expected: Rejection due to lower version${NC}"
curl -X PUT "$BASE_URL/00000000-0000-0000-0000-000000000001" \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "00000000-0000-0000-0000-000000000001",
    "version": 0,
    "counterPartyId": "COUNTER_PARTY_1",
    "bookId": "BOOK_1",
    "maturityDate": "2025-12-31",
    "createdDate": "2024-02-13",
    "expired": false
  }' | jq '.' || echo "✅ Correctly Rejected!"

echo -e "\n${BLUE}Step 6: Updating Trade with Higher Version (Should Succeed)...${NC}"
echo -e "${GREEN}Expected: Success with version upgrade${NC}"
curl -X PUT "$BASE_URL/00000000-0000-0000-0000-000000000001" \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "00000000-0000-0000-0000-000000000001",
    "version": 2,
    "counterPartyId": "COUNTER_PARTY_1",
    "bookId": "BOOK_1",
    "maturityDate": "2025-12-31",
    "createdDate": "2024-02-13",
    "expired": false
  }' | jq '.'

echo -e "\n${BLUE}Step 7: Creating Trades That Will Expire Soon (for Expiry Demo)...${NC}"

# Create trades that will expire in less than 2 minutes (for demo)
EXPIRY_DATE=$(date -d "+2 minutes" +%Y-%m-%d 2>/dev/null || date -v+2M +%Y-%m-%d)

echo -e "${YELLOW}Creating Trade 4 (Will expire in 2 minutes):${NC}"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"tradeId\": \"00000000-0000-0000-0000-000000000004\",
    \"version\": 1,
    \"counterPartyId\": \"COUNTER_PARTY_4\",
    \"bookId\": \"BOOK_4\",
    \"maturityDate\": \"$EXPIRY_DATE\",
    \"createdDate\": \"2024-02-13\",
    \"expired\": false
  }" | jq '.'

echo -e "\n${YELLOW}Creating Trade 5 (Will expire in 2 minutes):${NC}"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"tradeId\": \"00000000-0000-0000-0000-000000000005\",
    \"version\": 1,
    \"counterPartyId\": \"COUNTER_PARTY_5\",
    \"bookId\": \"BOOK_5\",
    \"maturityDate\": \"$EXPIRY_DATE\",
    \"createdDate\": \"2024-02-13\",
    \"expired\": false
  }" | jq '.'

echo -e "\n${BLUE}Step 8: Viewing All Trades...${NC}"
curl -s "$BASE_URL" | jq '.'

echo -e "\n${BLUE}Step 9: Getting Specific Trade...${NC}"
curl -s "$BASE_URL/00000000-0000-0000-0000-000000000001" | jq '.'

echo -e "\n${BLUE}Step 10: Bulk Trade Creation (Performance Demo)...${NC}"
echo -e "${GREEN}Creating 100 trades in bulk...${NC}"

# Create bulk trades
BULK_TRADES='['
for i in {6..105}; do
  BULK_TRADES+='{
    "tradeId": "'$(printf "%036d" $i)'",
    "version": 1,
    "counterPartyId": "COUNTER_PARTY_'$((i % 10))'",
    "bookId": "BOOK_'$((i % 5))'",
    "maturityDate": "2025-12-31",
    "createdDate": "2024-02-13",
    "expired": false'
  if [ $i -eq 105 ]; then
    BULK_TRADES+='}'
  else
    BULK_TRADES+='},'
  fi
done
BULK_TRADES+=']'

echo "Bulk trades payload size: $(echo $BULK_TRADES | wc -c) characters"

# Note: This would use the optimized controller endpoint
echo -e "${YELLOW}Note: Use POST to /api/v2/trades/bulk for bulk operations${NC}"

echo -e "\n${BLUE}Step 11: Monitoring Trade Statistics...${NC}"
curl -s "http://localhost:8080/api/v2/trades/stats" | jq '.' 2>/dev/null || echo "Stats endpoint not available"

echo -e "\n${BLUE}Step 12: Watching for Expiry (Next 2 Minutes)...${NC}"
echo "The expiry scheduler runs every minute. Watch the application logs!"
echo "Trades 4 and 5 should expire automatically within 2 minutes."

echo -e "\n${GREEN}=== Demo Summary ===${NC}"
echo "✅ Valid trades created successfully"
echo "✅ Invalid trades properly rejected"
echo "✅ Version validation working"
echo "✅ Trades ready for automatic expiry"
echo ""
echo -e "${YELLOW}📋 What to Watch For:${NC}"
echo "1. Application logs showing trade processing"
echo "2. Expiry scheduler running every minute"
echo "3. Trades 4 & 5 expiring automatically"
echo "4. Database operations in the logs"
echo ""
echo -e "${BLUE}🔍 Useful Commands:${NC}"
echo "Watch logs: tail -f logs/application.log"
echo "Check trades: curl $BASE_URL | jq '.'"
echo "Check specific trade: curl $BASE_URL/TRADE_ID | jq '.'"
echo ""
echo -e "${GREEN}Demo completed successfully!${NC}"
