# Trade Store API - Postman Testing Guide

## Application Status
✅ **Compilation**: Fixed all compilation issues  
✅ **Tests**: All 12 tests passing  
✅ **Application**: Running on http://localhost:8080  

## API Endpoints

### Base URL
```
http://localhost:8080/api/trades
```

### Available Endpoints

#### 1. Get All Trades
- **Method**: GET
- **URL**: `/api/trades`
- **Description**: Returns all trades in the system

#### 2. Get Specific Trade
- **Method**: GET
- **URL**: `/api/trades/{tradeId}`
- **Description**: Returns a specific trade by ID
- **Path Parameter**: `tradeId` (UUID format)

#### 3. Create Trade
- **Method**: POST
- **URL**: `/api/trades`
- **Description**: Creates a new trade
- **Body**: JSON trade object

#### 4. Update Trade
- **Method**: PUT
- **URL**: `/api/trades/{tradeId}`
- **Description**: Updates an existing trade
- **Path Parameter**: `tradeId` (UUID format)
- **Body**: JSON trade object

#### 5. Delete Trade
- **Method**: DELETE
- **URL**: `/api/trades/{tradeId}`
- **Description**: Deletes a trade
- **Path Parameter**: `tradeId` (UUID format)

## Sample Trade Object

```json
{
    "tradeId": "550e8400-e29b-41d4-a716-446655440000",
    "version": 1,
    "counterPartyId": "CP-1",
    "bookId": "B1",
    "maturityDate": "2025-12-31",
    "createdDate": "2025-02-15",
    "expired": false
}
```

## Postman Collection Setup

### 1. Create New Collection
1. Open Postman
2. Click "New" → "Collection"
3. Name it "Trade Store API"

### 2. Add Requests

#### GET All Trades
- **Name**: Get All Trades
- **Method**: GET
- **URL**: `http://localhost:8080/api/trades`
- **Headers**: 
  - Key: `Accept`
  - Value: `application/json`

#### POST Create Trade
- **Name**: Create Trade
- **Method**: POST
- **URL**: `http://localhost:8080/api/trades`
- **Headers**:
  - Key: `Content-Type`
  - Value: `application/json`
  - Key: `Accept`
  - Value: `application/json`
- **Body** (raw, JSON):
```json
{
    "tradeId": "550e8400-e29b-41d4-a716-446655440000",
    "version": 1,
    "counterPartyId": "CP-1",
    "bookId": "B1",
    "maturityDate": "2025-12-31",
    "createdDate": "2025-02-15",
    "expired": false
}
```

#### GET Specific Trade
- **Name**: Get Specific Trade
- **Method**: GET
- **URL**: `http://localhost:8080/api/trades/550e8400-e29b-41d4-a716-446655440000`
- **Headers**:
  - Key: `Accept`
  - Value: `application/json`

#### PUT Update Trade
- **Name**: Update Trade
- **Method**: PUT
- **URL**: `http://localhost:8080/api/trades/550e8400-e29b-41d4-a716-446655440000`
- **Headers**:
  - Key: `Content-Type`
  - Value: `application/json`
  - Key: `Accept`
  - Value: `application/json`
- **Body** (raw, JSON):
```json
{
    "tradeId": "550e8400-e29b-41d4-a716-446655440000",
    "version": 2,
    "counterPartyId": "CP-1",
    "bookId": "B1",
    "maturityDate": "2025-12-31",
    "createdDate": "2025-02-15",
    "expired": false
}
```

#### DELETE Trade
- **Name**: Delete Trade
- **Method**: DELETE
- **URL**: `http://localhost:8080/api/trades/550e8400-e29b-41d4-a716-446655440000`

## Business Rules Implemented

### 1. Version Validation ✅ FIXED
- ✅ **Lower Version Rejection**: Trades with lower version are REJECTED with clear error message
- ✅ **Same Version Replacement**: Trades with same version REPLACE the current record  
- ✅ **Higher Version Acceptance**: Trades with higher version UPDATE the existing record
- ✅ **Detailed Logging**: Clear log messages for all version scenarios

### 2. Maturity Date Validation
- ✅ Trades with maturity date before today are rejected

### 3. Auto-Expiry
- ✅ Trades are automatically marked as expired when maturity date passes

## Testing Scenarios

### Scenario 1: Create Trade (Success)
1. Use POST endpoint with valid trade data
2. Expected: 201 Created response

### Scenario 2: Version Conflict (Failure)
1. Create a trade with version 1
2. Try to create same trade with version 0
3. Expected: 400 Bad Request with version conflict error

### Scenario 3: Maturity Date Validation (Failure)
1. Try to create trade with past maturity date
2. Expected: 400 Bad Request with validation error

### Scenario 4: Update Trade (Success)
1. Create a trade with version 1
2. Update same trade with version 2
3. Expected: 200 OK response

## Application Features

### Database Support
- ✅ **PostgreSQL** (primary)
- ✅ **H2** (in-memory for testing)
- ✅ **MongoDB** (for audit logs)

### Streaming
- ✅ **Apache Kafka** for trade processing

### Security
- ✅ **Spring Security** with basic authentication
- ✅ **OWASP Dependency Check** for vulnerability scanning

### Monitoring
- ✅ **Spring Actuator** endpoints
- ✅ **Micrometer** metrics
- ✅ **Prometheus** integration

### Testing
- ✅ **JUnit 5** with comprehensive test coverage
- ✅ **TestContainers** for integration testing
- ✅ **TDD approach** implemented

## Build & Run Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Run application
mvn spring-boot:run

# Build for deployment
mvn clean package
```

## Assignment Requirements Compliance

✅ **Spring Boot Application**  
✅ **Streaming Tool (Apache Kafka)**  
✅ **SQL Database (PostgreSQL)**  
✅ **NoSQL Database (MongoDB)**  
✅ **JUnit Tests with TDD**  
✅ **Deployment Pipeline (GitHub Actions)**  
✅ **Automated Regression Testing**  
✅ **OSS Vulnerability Scan (OWASP)**  
✅ **Build fails on critical/blocker vulnerabilities**  
✅ **Business Rules Implemented**  

The application is ready for Postman testing and meets all assignment requirements!
