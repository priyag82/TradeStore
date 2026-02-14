# Environment Variables Setup

This document outlines the required environment variables for secure deployment of TradeStore application.

## 🔐 **Security Configuration**

### Required Environment Variables:
```bash
export SECURITY_USERNAME=admin
export SECURITY_ADMIN_PASSWORD=your_secure_password_here
```

### Purpose:
- `SECURITY_USERNAME`: Admin username for Spring Security
- `SECURITY_ADMIN_PASSWORD`: Admin password for Spring Security

## 🗄️ **Database Configuration**

### Required Environment Variables:
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/tradestore_prod
export DATABASE_USERNAME=tradestore_user
export DATABASE_PASSWORD=your_db_password_here
export DATABASE_DRIVER=org.postgresql.Driver
```

### Development Defaults:
- **H2 Database**: `jdbc:h2:mem:testdb` (in-memory)
- **Username**: `sa`
- **Password**: (empty)

## 🍃 **MongoDB Configuration**

### Required Environment Variables:
```bash
export MONGODB_DATABASE=tradestore_audit_prod
export MONGODB_URI=mongodb://localhost:27017/tradestore_audit_prod
```

### Development Defaults:
- **Database**: `tradestore_audit_dev`
- **Connection**: `mongodb://localhost:27017`

## 📨 **Kafka Configuration**

### Required Environment Variables:
```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_ENABLED=true
```

### Development Defaults:
- **Kafka**: Disabled (`false`)
- **Bootstrap Servers**: Not configured

## 🖥️ **H2 Console Configuration**

### Required Environment Variables:
```bash
export H2_CONSOLE_ENABLED=true
```

### Access URL:
- **Development**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: (empty)

## 🚀 **Production Deployment Example**

```bash
# Security
export SECURITY_USERNAME=admin
export SECURITY_ADMIN_PASSWORD=SuperSecurePassword123!

# Database (PostgreSQL)
export DATABASE_URL=jdbc:postgresql://prod-db:5432/tradestore_prod
export DATABASE_USERNAME=tradestore_user
export DATABASE_PASSWORD=ProdDBPassword456!
export DATABASE_DRIVER=org.postgresql.Driver

# MongoDB
export MONGODB_DATABASE=tradestore_audit_prod
export MONGODB_URI=mongodb://prod-mongo:27017/tradestore_audit_prod

# Kafka
export KAFKA_BOOTSTRAP_SERVERS=prod-kafka:9092
export KAFKA_ENABLED=true

# H2 Console (disabled in production)
export H2_CONSOLE_ENABLED=false
```

## ⚠️ **Security Notes**

1. **Never commit passwords** to version control
2. **Use environment variables** for all sensitive data
3. **Rotate passwords regularly** in production
4. **Use strong passwords** with minimum 12 characters
5. **Monitor access logs** for unauthorized attempts

## 🔍 **Verification**

To verify no plain text passwords:
```bash
# Check for hardcoded passwords
grep -r "password.*=" src/main/resources/ --exclude="README.md"

# Should return no results for production configuration
```

## 📋 **Development Quick Start**

For local development with default values:
```bash
# Set only admin password (required)
export SECURITY_ADMIN_PASSWORD=dev123

# Run application
mvn spring-boot:run
```

This ensures secure deployment while maintaining development convenience.
