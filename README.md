# Trade Store Service

A Spring Boot 3 application implementing a Trade Store Service with Polyglot Persistence strategy using PostgreSQL for trade data and MongoDB for audit logging.

## Architecture Overview

This service follows Test-Driven Development (TDD) principles and implements:
- **Trade Validation**: Version checking and maturity date validation
- **Polyglot Persistence**: PostgreSQL for official trade book, MongoDB for raw message auditing
- **Event-Driven Architecture**: Kafka integration for real-time trade processing
- **Scheduled Tasks**: Automatic trade expiry marking

## Technology Stack

- **Java 17** with Spring Boot 3.2.0
- **PostgreSQL** - Primary trade data storage
- **MongoDB** - Audit message storage
- **Apache Kafka** - Message streaming
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **Testcontainers** - Integration testing
- **Maven** - Build management

## Features

### Trade Validation Rules
1. **Version Check**: Reject trades with lower versions than existing ones
2. **Same Version**: Replace existing trade when version is identical
3. **Maturity Date**: Reject trades with maturity dates before today
4. **Expiry Logic**: Automatically mark trades as expired when maturity date is surpassed

### Persistence Strategy
- **PostgreSQL**: Stores validated trade entities
- **MongoDB**: Stores raw Kafka messages for auditing
- **Atomic Operations**: Each trade is processed with proper transaction management

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

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

### Running Tests

```bash
# Run all tests
mvn test

# Run integration tests
mvn verify -P integration-tests

# Generate test coverage report
mvn jacoco:report
```

## API Usage

### Sending Trade Messages

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

### PostgreSQL (trades table)
```sql
CREATE TABLE trades (
    trade_id UUID PRIMARY KEY,
    version INTEGER NOT NULL,
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
# Database connections
spring.datasource.url=jdbc:postgresql://localhost:5432/tradestore
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017

# Kafka configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=trade-store-group
```

### Environment Variables
Override configuration with environment variables:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATA_MONGODB_HOST`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`

## Monitoring & Observability

### Health Endpoints
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

### Logging
Configured log levels:
- `com.tradestore` - INFO
- `org.springframework.kafka` - INFO
- `org.hibernate.SQL` - DEBUG

## CI/CD Pipeline

The GitHub Actions pipeline includes:

1. **Automated Testing**
   - Unit tests with JUnit 5
   - Integration tests with Testcontainers
   - Test coverage reporting

2. **Security Scanning**
   - OWASP Dependency Check
   - Trivy vulnerability scanner
   - Docker image security scanning
   - Fails on critical/blocker vulnerabilities

3. **Build & Deployment**
   - Maven build
   - Docker image creation
   - Deployment to staging (main branch only)

## Development

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

## PlantUML Diagrams

### Sequence Diagram
View the trade processing flow:
```bash
# Generate PNG from PlantUML
plantuml docs/sequence-diagram.puml
```

### Class Diagram
View the domain model:
```bash
# Generate PNG from PlantUML
plantuml docs/class-diagram.puml
```

## Troubleshooting

### Common Issues

1. **Kafka Connection Failed**
   - Ensure Kafka is running: `docker-compose ps`
   - Check port availability: 9092

2. **Database Connection Issues**
   - Verify PostgreSQL is accessible on port 5432
   - Check MongoDB is accessible on port 27017
   - Validate database credentials

3. **Test Failures**
   - Ensure all services are running for integration tests
   - Check testcontainers Docker daemon access

### Logs

View application logs:
```bash
# Docker logs
docker-compose logs -f tradestore-app

# Maven logs
mvn spring-boot:run
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests first (TDD)
4. Implement functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License.
