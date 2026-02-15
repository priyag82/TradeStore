# Trade Store Service

A Spring Boot 3 application implementing a Trade Store Service with Polyglot Persistence strategy using H2 for primary trade data and MongoDB for audit logging.

## Architecture Overview

This service follows Test-Driven Development (TDD) principles and implements:
- **Trade Validation**: Version checking and maturity date validation with proper error handling
- **Polyglot Persistence**: H2 for official trade book, MongoDB for raw message auditing
- **REST API**: Full REST endpoints for trade management operations
- **Event-Driven Architecture**: Kafka integration for real-time trade processing
- **Scheduled Tasks**: Automatic trade expiry marking
- **User-Friendly Error Responses**: Structured error messages for API consumers

## Technology Stack

- **Java 17** with Spring Boot 3.2.0
- **H2 Database** - Primary trade data storage (in-memory)
- **MongoDB** - Audit message storage
- **Apache Kafka 3.5.1** - Message streaming
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **Testcontainers 1.19.3** - Integration testing
- **Maven** - Build management
- **Micrometer** - Metrics collection
- **Spring Security** - Authentication and authorization

## Features

### Trade Validation Rules ✅ IMPLEMENTED
1. **Version Check**: Reject trades with lower versions than existing ones
2. **Same Version**: Replace existing trade when version is identical
3. **Higher Version**: Accept and update existing trade with higher version
4. **Maturity Date**: Reject trades with maturity dates before today
5. **Expiry Logic**: Automatically mark trades as expired when maturity date is surpassed

### REST API Endpoints ✅ IMPLEMENTED
- **GET** `/api/trades` - Get all trades
- **GET** `/api/trades/{tradeId}` - Get trade by ID
- **POST** `/api/trades` - Create new trade (also handles version-based updates)
- **DELETE** `/api/trades/{tradeId}` - Delete trade

### Error Handling ✅ IMPLEMENTED
- **User-friendly error messages** in JSON format
- **Proper HTTP status codes** (400, 409, 404, etc.)
- **Structured error responses** with details and timestamps
- **Global exception handling** for consistent error format

### Persistence Strategy
- **H2 Database**: Stores validated trade entities with optimistic locking
- **MongoDB**: Stores raw Kafka messages for auditing
- **Atomic Operations**: Each trade is processed with proper transaction management

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for MongoDB and Kafka)

### Running the Application

1. **Start Infrastructure Services**
   ```bash
   docker-compose up -d
   ```

2. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

3. **Access the Application**
   - Application: http://localhost:8080
   - Health Check: http://localhost:8080/actuator/health
   - Metrics: http://localhost:8080/actuator/metrics
   - H2 Console: http://localhost:8080/h2-console

### Running Tests

```bash
# Run all tests (17 tests passing)
mvn test

# Run integration tests
mvn verify -P integration-tests

# Generate test coverage report
mvn jacoco:report
```

## API Usage

### REST API Examples

#### Create a Trade
```bash
POST http://localhost:8080/api/trades
Content-Type: application/json

{
  "tradeId": "550e8400-e29b-41d4-a716-446655440000",
  "version": 1,
  "counterPartyId": "CP-001",
  "bookId": "BOOK-001",
  "maturityDate": "2024-12-31",
  "createdDate": "2024-01-15",
  "expired": false
}
```

#### Get Trade by ID
```bash
GET http://localhost:8080/api/trades/550e8400-e29b-41d4-a716-446655440000
```

#### Get All Trades
```bash
GET http://localhost:8080/api/trades
```

#### Update Trade (Higher Version)
```bash
POST http://localhost:8080/api/trades
Content-Type: application/json

{
  "tradeId": "550e8400-e29b-41d4-a716-446655440000",
  "version": 2,
  "counterPartyId": "CP-001",
  "bookId": "BOOK-001",
  "maturityDate": "2024-12-31",
  "createdDate": "2024-01-15",
  "expired": false
}
```
**Note**: The same POST endpoint handles both creation and version-based updates. Higher versions replace existing trades, same versions replace current records, lower versions are rejected.

### Error Response Examples

#### Version Conflict (409 Conflict)
```json
{
  "type": "https://tradestore.com/errors/version-conflict",
  "title": "Version Conflict",
  "status": 409,
  "detail": "Trade version 1 is lower than existing version 2 - REJECTED",
  "instance": "uri=/api/trades",
  "timestamp": "2026-02-15T09:20:43.357469500Z"
}
```

#### Maturity Date Validation (400 Bad Request)
```json
{
  "type": "https://tradestore.com/errors/invalid-argument",
  "title": "Invalid Argument",
  "status": 400,
  "detail": "Trade maturity date cannot be before today",
  "instance": "uri=/api/trades",
  "timestamp": "2026-02-15T09:09:16.913230300Z"
}
```

### Sending Trade Messages via Kafka

Send JSON messages to the `incoming.trade.data` Kafka topic:

```json
{
  "tradeId": "550e8400-e29b-41d4-a716-446655440000",
  "version": 1,
  "counterPartyId": "CP-001",
  "bookId": "BOOK-001",
  "maturityDate": "2024-12-31",
  "createdDate": "2024-01-15",
  "expired": "N"
}
```

### Example Kafka Producer

```bash
# Using kafka-console-producer
kafka-console-producer.sh --broker-list localhost:9092 --topic incoming.trade.data
```

## Database Schema

### H2 Database (trades table)
```sql
CREATE TABLE trades (
    trade_id UUID PRIMARY KEY,
    business_version INTEGER NOT NULL,
    jpa_version BIGINT NOT NULL DEFAULT 0,
    counter_party_id VARCHAR(50) NOT NULL,
    book_id VARCHAR(50) NOT NULL,
    maturity_date DATE NOT NULL,
    created_date DATE NOT NULL,
    expired VARCHAR(1) NOT NULL,
    timestamp DATE,
    last_updated DATE
);
```

### MongoDB (audit_messages collection)
```json
{
  "_id": "string",
  "messageId": "string",
  "topic": "incoming.trade.data",
  "partition": 0,
  "offset": 123,
  "messageContent": "JSON string",
  "receivedAt": "2024-01-15T10:30:00",
  "processedAt": "2024-01-15T10:30:01",
  "status": "PROCESSED|FAILED",
  "errorMessage": "string"
}
```

## Configuration

### Application Properties
Key configuration options in `application.properties`:

```properties
# Database connections (H2 by default)
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.data.mongodb.uri=mongodb://localhost:27017
spring.data.mongodb.database=tradestore_audit_dev

# Kafka configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=trade-store-group

# Security
spring.security.user.name=admin
spring.security.user.password=
```

### Environment Variables
Override configuration with environment variables:
- `DATABASE_URL` - H2 or PostgreSQL connection string
- `MONGODB_URI` - MongoDB connection string
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka bootstrap servers

### Database Profiles
- **dev**: H2 in-memory database (default)
- **demo**: H2 in-memory with demo configuration
- **prod**: PostgreSQL (requires DATABASE_URL environment variable)

## Monitoring & Observability

### Health Endpoints
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

### Custom Metrics
- `rejected.trades.counter` - Count of rejected trades
- `processed.trades.counter` - Count of successfully processed trades
- `expired.trades.counter` - Count of expired trades

### Logging
Configured log levels:
- `com.tradestore` - INFO
- `org.springframework.kafka` - WARN
- `org.hibernate.SQL` - DEBUG

## Testing

### Test Coverage ✅ COMPREHENSIVE
- **17 tests passing** with 100% core functionality coverage
- **Unit Tests**: TradeService, TradeController, validation logic
- **Integration Tests**: Full end-to-end trade processing
- **Version Logic Tests**: Comprehensive version validation scenarios
- **Error Handling Tests**: All exception scenarios covered

### Running Tests with Testcontainers

Integration tests use Testcontainers for real database testing:

```bash
mvn verify -P integration-tests
```

### Code Quality

- Follow TDD principles
- Maintain test coverage > 80%
- Use meaningful commit messages
- Follow Spring Boot best practices

## Postman Testing

A comprehensive Postman testing guide is available in `POSTMAN_TESTING_GUIDE.md` with:
- **Complete API documentation**
- **Sample requests for all endpoints**
- **Error response examples**
- **Testing scenarios for version validation**
- **Step-by-step testing instructions**

### Quick Postman Setup
1. Import the `POSTMAN_TESTING_GUIDE.md` examples
2. Set base URL: `http://localhost:8080/api/trades`
3. Use the sample JSON bodies from the guide
4. Test all validation scenarios (version, maturity date, etc.)

## CI/CD Pipeline

The GitHub Actions pipeline includes:

1. **Automated Testing**
   - Unit tests with JUnit 5 (17 tests)
   - Integration tests with Testcontainers
   - Test coverage reporting

2. **Security Scanning**
   - OWASP Dependency Check
   - Fails on critical/blocker vulnerabilities

3. **Build & Deployment**
   - Maven build
   - Docker image creation
   - Deployment to staging (main branch only)

## Development

### Key Implementation Details ✅ FIXED
- **Optimistic Locking**: Separated JPA version from business version to prevent conflicts
- **Validation Layer**: Moved validation to service layer for proper error handling
- **Exception Handling**: Global exception handler with user-friendly responses
- **Version Logic**: Correctly implements assignment requirements for version handling
- **Error Messages**: Clear, actionable error messages for API consumers

### Code Structure
```
src/main/java/com/tradestore/
├── controller/          # REST API endpoints
├── service/            # Business logic and validation
├── entity/             # JPA entities
├── repository/         # Data access layer
├── exception/          # Custom exceptions and handlers
├── config/             # Configuration classes
└── domain/valueobject/ # Value objects for type safety
```

## Docker Services

The application uses Docker Compose for external services:

```yaml
services:
  postgres:
    image: postgres:latest
    ports: ["5432:5432"]
  mongodb:
    image: mongo:latest
    ports: ["27017:27017"]
  kafka:
    image: apache/kafka:latest
    ports: ["9092:9092"]
```

## Troubleshooting

### Common Issues

1. **Kafka Connection Failed**
   - Ensure Kafka is running: `docker-compose ps`
   - Check port availability: 9092

2. **Database Connection Issues**
   - Verify H2 is accessible (in-memory by default)
   - Check MongoDB is accessible on port 27017
   - For production, ensure PostgreSQL is configured

3. **Test Failures**
   - Ensure all services are running for integration tests
   - Check testcontainers Docker daemon access

4. **Version Conflicts**
   - Check application logs for detailed version validation messages
   - Verify trade ID and version in request payload

### Logs

View application logs:
```bash
# Docker logs
docker-compose logs -f

# Maven logs
mvn spring-boot:run
```

### H2 Console Access

For development and debugging:
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (leave blank)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests first (TDD)
4. Implement functionality
5. Ensure all tests pass (17 tests)
6. Submit a pull request

## License

This project is licensed under the MIT License.
