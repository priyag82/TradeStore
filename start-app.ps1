# TradeStore Startup Script
# This script starts the application with UTC timezone to avoid PostgreSQL issues

Write-Host "Starting TradeStore Application..." -ForegroundColor Green

# Force JVM to use UTC timezone at multiple levels
$env:JAVA_OPTS = "-Duser.timezone=UTC"
$env:TZ = "UTC"

# Start the application with explicit JVM timezone
Write-Host "Running: mvn spring-boot:run with UTC timezone" -ForegroundColor Blue
mvn spring-boot:run

Write-Host "TradeStore stopped" -ForegroundColor Yellow
