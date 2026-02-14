@echo off
echo 🚀 Testing Trade Store Application
echo ==================================

echo.
echo ✅ Step 1: Creating a valid trade...
curl -X POST http://localhost:8080/api/trades ^
  -H "Content-Type: application/json" ^
  -d "{\"tradeId\": \"00000000-0000-0000-0000-000000000001\", \"version\": 1, \"counterPartyId\": \"COUNTER_PARTY_1\", \"bookId\": \"BOOK_1\", \"maturityDate\": \"2025-12-31\", \"createdDate\": \"2024-02-13\", \"expired\": false}"

echo.
echo.
echo ✅ Step 2: Creating another valid trade...
curl -X POST http://localhost:8080/api/trades ^
  -H "Content-Type: application/json" ^
  -d "{\"tradeId\": \"00000000-0000-0000-0000-000000000002\", \"version\": 1, \"counterPartyId\": \"COUNTER_PARTY_2\", \"bookId\": \"BOOK_2\", \"maturityDate\": \"2025-06-30\", \"createdDate\": \"2024-02-13\", \"expired\": false}"

echo.
echo.
echo ❌ Step 3: Creating invalid trade (past date)...
curl -X POST http://localhost:8080/api/trades ^
  -H "Content-Type: application/json" ^
  -d "{\"tradeId\": \"00000000-0000-0000-0000-000000000003\", \"version\": 1, \"counterPartyId\": \"COUNTER_PARTY_3\", \"bookId\": \"BOOK_3\", \"maturityDate\": \"2023-01-01\", \"createdDate\": \"2024-02-13\", \"expired\": false}"

echo.
echo.
echo ✅ Step 4: Viewing all trades...
curl http://localhost:8080/api/trades

echo.
echo.
echo 🎉 Test completed! Check the application logs for details.
pause
