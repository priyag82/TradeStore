#!/bin/bash

# PostgreSQL Setup Script for TradeStore
# This script sets up PostgreSQL database for TradeStore application

echo "🚀 Setting up PostgreSQL for TradeStore..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Stop and remove existing container if it exists
if docker ps -a | grep -q tradestore-postgres; then
    echo "🔄 Removing existing PostgreSQL container..."
    docker stop tradestore-postgres
    docker rm tradestore-postgres
fi

# Create and start PostgreSQL container
echo "🐳 Starting PostgreSQL container..."
docker run --name tradestore-postgres \
    -e POSTGRES_DB=tradestore \
    -e POSTGRES_USER=tradestore_user \
    -e POSTGRES_PASSWORD=tradestore_pass \
    -p 5432:5432 \
    -d postgres:15

# Wait for PostgreSQL to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
sleep 10

# Check if PostgreSQL is ready
for i in {1..30}; do
    if docker exec tradestore-postgres pg_isready -U tradestore_user -d tradestore; then
        echo "✅ PostgreSQL is ready!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ PostgreSQL failed to start. Please check logs:"
        docker logs tradestore-postgres
        exit 1
    fi
    sleep 2
done

# Create additional databases
echo "📊 Creating additional databases..."
docker exec tradestore-postgres psql -U tradestore_user -d tradestore -c "CREATE DATABASE tradestore_dev;"
docker exec tradestore-postgres psql -U tradestore_user -d tradestore -c "CREATE DATABASE tradestore_demo;"

echo "✅ PostgreSQL setup complete!"
echo ""
echo "🔗 Connection Details:"
echo "   Host: localhost"
echo "   Port: 5432"
echo "   User: tradestore_user"
echo "   Password: tradestore_pass"
echo "   Databases: tradestore, tradestore_dev, tradestore_demo"
echo ""
echo "🎯 Next steps:"
echo "   1. Run: mvn spring-boot:run"
echo "   2. Test: curl http://localhost:8080/actuator/health"
echo "   3. Create a test trade via API"
echo ""
echo "🛑 To stop PostgreSQL: docker stop tradestore-postgres"
echo "🗑️  To remove PostgreSQL: docker rm tradestore-postgres"
