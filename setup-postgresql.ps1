# PostgreSQL Setup Script for TradeStore (PowerShell)
# This script connects to existing PostgreSQL container

Write-Host "Setting up TradeStore for existing PostgreSQL..." -ForegroundColor Green

# Check if PostgreSQL container is running
try {
    $container = docker ps --filter "name=tradestore-postgres-1" --format "{{.Names}}"
    if ($container -eq "tradestore-postgres-1") {
        Write-Host "Found existing PostgreSQL container: tradestore-postgres-1" -ForegroundColor Green
    } else {
        Write-Host "PostgreSQL container 'tradestore-postgres-1' not found. Please start it first." -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Docker is not running. Please start Docker first." -ForegroundColor Red
    exit 1
}

# Wait for PostgreSQL to be ready
Write-Host "Checking PostgreSQL connection..." -ForegroundColor Yellow

# Check if PostgreSQL is ready
for ($i = 1; $i -le 30; $i++) {
    try {
        $result = docker exec tradestore-postgres-1 pg_isready -U user -d tradestore
        if ($LASTEXITCODE -eq 0) {
            Write-Host "PostgreSQL is ready!" -ForegroundColor Green
            break
        }
    } catch {
        # Continue trying
    }
    
    if ($i -eq 30) {
        Write-Host "PostgreSQL is not ready. Please check container logs:" -ForegroundColor Red
        docker logs tradestore-postgres-1
        exit 1
    }
    Start-Sleep -Seconds 2
}

# Create additional databases if they don't exist
Write-Host "Creating additional databases..." -ForegroundColor Blue

# Create tradestore_dev database
docker exec tradestore-postgres-1 psql -U user -d tradestore -c "SELECT 1 FROM pg_database WHERE datname='tradestore_dev'" | Out-Null
if ($LASTEXITCODE -ne 0) {
    docker exec tradestore-postgres-1 psql -U user -d tradestore -c "CREATE DATABASE tradestore_dev;" | Out-Null
    Write-Host "Created tradestore_dev database" -ForegroundColor Green
} else {
    Write-Host "tradestore_dev database already exists" -ForegroundColor Yellow
}

# Create tradestore_demo database
docker exec tradestore-postgres-1 psql -U user -d tradestore -c "SELECT 1 FROM pg_database WHERE datname='tradestore_demo'" | Out-Null
if ($LASTEXITCODE -ne 0) {
    docker exec tradestore-postgres-1 psql -U user -d tradestore -c "CREATE DATABASE tradestore_demo;" | Out-Null
    Write-Host "Created tradestore_demo database" -ForegroundColor Green
} else {
    Write-Host "tradestore_demo database already exists" -ForegroundColor Yellow
}

Write-Host "PostgreSQL setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Connection Details:" -ForegroundColor Cyan
Write-Host "   Host: localhost"
Write-Host "   Port: 5432"
Write-Host "   User: user"
Write-Host "   Password: password"
Write-Host "   Container: tradestore-postgres-1"
Write-Host "   Databases: tradestore, tradestore_dev, tradestore_demo"
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "   1. Run: mvn spring-boot:run"
Write-Host "   2. Test: curl http://localhost:8080/actuator/health"
Write-Host "   3. Create a test trade via API"
Write-Host ""
Write-Host "To check container status: docker ps" -ForegroundColor Yellow
Write-Host "To view container logs: docker logs tradestore-postgres-1" -ForegroundColor Yellow
