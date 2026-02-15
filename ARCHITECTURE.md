# Trade Store Service - High Level Architecture

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           TRADE STORE SERVICE                                   │
│                              (Spring Boot 3.2.0)                               │
└─────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ HTTP REST API
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            CONTROLLER LAYER                                     │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                        TradeController                                  │    │
│  │                                                                         │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │    │
│  │  │ GET /trades │  │ POST /trades│  │ GET /trades/│  │ DELETE     │      │    │
│  │  │             │  │             │  │ {id}        │  │ /trades/{id}│      │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘      │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                    GlobalExceptionHandler                                  │    │
│  │                                                                         │    │
│  │  • VersionConflictException → 409 Conflict                             │    │
│  │  • IllegalArgumentException → 400 Bad Request                            │    │
│  │  • Structured JSON Error Responses                                      │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ Business Logic
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                             SERVICE LAYER                                       │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                         TradeService                                     │    │
│  │                                                                         │    │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐          │    │
│  │  │  Version Logic  │  │ Maturity Date  │  │  Expiry Task    │          │    │
│  │  │                 │  │   Validation    │  │                 │          │    │
│  │  │ • Higher → Update│  │ • Reject Past  │  │ • Scheduled     │          │    │
│  │  │ • Same → Replace│  │ • Before Today │  │ • Auto-Expire   │          │    │
│  │  │ • Lower → Reject│  │ • Service Layer│  │ • Daily Check   │          │    │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘          │    │
│  │                                                                         │    │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐          │    │
│  │  │   Micrometer    │  │   Optimistic    │  │   Transaction   │          │    │
│  │  │   Metrics       │  │   Locking       │  │   Management    │          │    │
│  │  │                 │  │                 │  │                 │          │    │
│  │  │ • Processed     │  │ • JPA Version   │  │ • @Transactional│          │    │
│  │  │ • Rejected      │  │ • Business Ver  │  │ • Rollback      │          │    │
│  │  │ • Expired       │  │ • Conflict Det  │  │ • Consistency   │          │    │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘          │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                    KafkaTradeConsumer (Optional)                         │    │
│  │                                                                         │    │
│  │  • @ConditionalOnProperty(kafka.enabled=true)                           │    │
│  │  • Processes messages from incoming.trade.data                           │    │
│  │  • Stores audit logs in MongoDB                                          │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ Data Access
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            REPOSITORY LAYER                                    │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                      JPA Repositories                                     │    │
│  │                                                                         │    │
│  │  ┌─────────────────────────────────────────────────────────────────┐    │    │
│  │  │                    TradeRepository                               │    │    │
│  │  │                                                                 │    │    │
│  │  │  • findById()      • save()    • findAll()                       │    │    │
│  │  │  • deleteById()    • exists()  • Custom Queries                  │    │    │
│  │  └─────────────────────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                   MongoDB Repositories                                  │    │
│  │                                                                         │    │
│  │  ┌─────────────────────────────────────────────────────────────────┐    │    │
│  │  │                 AuditMessageRepository                           │    │    │
│  │  │                                                                 │    │    │
│  │  │  • save()          • findAll()  • findByTopic()                  │    │    │
│  │  │  • findByStatus()  • delete()  • Custom Queries                  │    │    │
│  │  └─────────────────────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ Data Storage
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            DATA STORAGE LAYER                                   │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                          H2 DATABASE                                     │    │
│  │                           (Primary Store)                                  │    │
│  │                                                                         │    │
│  │  ┌─────────────────────────────────────────────────────────────────┐    │    │
│  │  │                           TRADES TABLE                           │    │    │
│  │  │                                                                 │    │    │
│  │  │  • trade_id (UUID, PK)                                           │    │    │
│  │  │  • business_version (INTEGER)                                    │    │    │
│  │  │  • jpa_version (BIGINT, @Version)                                │    │    │
│  │  │  • counter_party_id, book_id                                     │    │    │
│  │  │  • maturity_date, created_date                                  │    │    │
│  │  │  • expired (BOOLEAN)                                            │    │    │
│  │  │  • timestamp, last_updated                                       │    │    │
│  │  └─────────────────────────────────────────────────────────────────┘    │    │
│  │                                                                         │    │
│  │  • In-memory by default                                              │    │
│  │  • H2 Console available at /h2-console                              │    │
│  │  • Auto-create/drop schema                                          │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                          MONGODB                                         │    │
│  │                        (Audit Storage)                                    │    │
│  │                                                                         │    │
│  │  ┌─────────────────────────────────────────────────────────────────┐    │    │
│  │  │                    audit_messages COLLECTION                     │    │    │
│  │  │                                                                 │    │    │
│  │  │  • _id, messageId, topic, partition, offset                      │    │    │
│  │  │  • messageContent (JSON string)                                 │    │    │
│  │  │  • receivedAt, processedAt, status, errorMessage                │    │    │
│  │  └─────────────────────────────────────────────────────────────────┘    │    │
│  │                                                                         │    │
│  │  • Optional (requires Docker)                                        │    │
│  │  • Kafka message auditing                                             │    │
│  │  • Raw message storage                                               │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ External Services (Optional)
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                        EXTERNAL SERVICES (DOCKER)                               │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                          KAFKA                                            │    │
│  │                      (Message Streaming)                                   │    │
│  │                                                                         │    │
│  │  • Topic: incoming.trade.data                                          │    │
│  │  • Disabled by default (kafka.enabled=false)                            │    │
│  │  • Apache Kafka 3.5.1                                                  │    │
│  │  • Port: 9092                                                         │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                          MONGODB                                         │    │
│  │                      (Document Database)                                  │    │
│  │                                                                         │    │
│  │  • Port: 27017                                                        │    │
│  │  • Database: tradestore_audit_dev                                      │    │
│  │  • Auto-index creation                                                │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                          POSTGRESQL                                       │    │
│  │                     (Production Database)                                  │    │
│  │                                                                         │    │
│  │  • Available via docker-compose                                         │    │
│  │  • Port: 5432                                                         │    │
│  │  • For production profile only                                          │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────────┘

## Cross-Cutting Concerns

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                        CROSS-CUTTING CONCERNS                                │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                          SECURITY                                        │    │
│  │                                                                         │    │
│  │  • Spring Security with Basic Auth                                     │    │
│  │  • Admin credentials (configurable)                                     │    │
│  │  • Role-based access control                                           │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                        MONITORING                                        │    │
│  │                                                                         │    │
│  │  • Spring Boot Actuator                                               │    │
│  │  • /actuator/health, /actuator/metrics                               │    │
│  │  • Micrometer custom counters                                         │    │
│  │  • Prometheus metrics export                                          │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                         LOGGING                                          │    │
│  │                                                                         │    │
│  │  • SLF4J with Logback                                                  │    │
│  │  • Structured logging with trade IDs                                   │    │
│  │  • Configurable log levels                                             │    │
│  │  • Hibernate SQL debugging                                              │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                         CONFIGURATION                                     │    │
│  │                                                                         │    │
│  │  • Profile-based configuration (dev/demo/prod)                          │    │
│  │  • Environment variable overrides                                       │    │
│  │  • Conditional bean creation (@ConditionalOnProperty)                    │    │
│  │  • Externalized configuration                                           │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## Data Flow

### 1. REST API Flow
```
Client Request → TradeController → TradeService → TradeRepository → H2 Database
                     ↓                    ↓                ↓
               Exception Handler ← Validation Logic ← JPA Operations
                     ↓
               Structured Error Response
```

### 2. Version Validation Flow
```
Trade with Version → TradeService.validateVersion()
                           ↓
            Check existing trade in database
                           ↓
    ┌─────────────────┬─────────────────┬─────────────────┐
    │ Lower Version   │ Same Version    │ Higher Version  │
    │ → REJECT        │ → REPLACE      │ → UPDATE       │
    └─────────────────┴─────────────────┴─────────────────┘
```

### 3. Kafka Flow (Optional)
```
Kafka Topic → KafkaTradeConsumer → TradeService → MongoDB Audit
                    ↓
               Message Validation
                    ↓
               Trade Processing
                    ↓
               Audit Log Storage
```

## Key Design Patterns

1. **Layered Architecture**: Clear separation of concerns across layers
2. **Repository Pattern**: Data access abstraction
3. **Service Layer Pattern**: Business logic encapsulation
4. **Global Exception Handling**: Centralized error processing
5. **Conditional Configuration**: Optional features based on properties
6. **Optimistic Locking**: Concurrent modification prevention
7. **Polyglot Persistence**: Multiple databases for different purposes

## Technology Stack Summary

- **Application Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Primary Database**: H2 (in-memory)
- **Audit Database**: MongoDB (optional)
- **Message Queue**: Apache Kafka 3.5.1 (optional)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Security**: Spring Security
- **Monitoring**: Spring Boot Actuator, Micrometer
