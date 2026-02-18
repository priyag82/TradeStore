# TradeStore Startup Script with Timezone Fix
# This script starts the application with proper timezone settings

Write-Host "Starting TradeStore with timezone fix..." -ForegroundColor Green

# Set JVM timezone to use Kolkata (new name for Calcutta)
$env:JAVA_OPTS = "-Duser.timezone=UTC"

# Start the application
Write-Host "Running: mvn spring-boot:run with timezone fix" -ForegroundColor Blue
mvn spring-boot:run

Write-Host "TradeStore stopped" -ForegroundColor Yellow
