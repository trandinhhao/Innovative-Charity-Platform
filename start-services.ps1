# PowerShell script to start all required services

Write-Host "Starting Charity Backend Services..." -ForegroundColor Green

# Check if Docker is running
try {
    docker ps | Out-Null
} catch {
    Write-Host "Error: Docker is not running. Please start Docker Desktop first." -ForegroundColor Red
    exit 1
}

# Start MySQL
Write-Host "`n[1/3] Starting MySQL..." -ForegroundColor Yellow
$mysqlRunning = docker ps -a --filter "name=charity-mysql" --format "{{.Status}}" | Select-String "Up"
if ($mysqlRunning) {
    Write-Host "MySQL is already running" -ForegroundColor Green
} else {
    $mysqlExists = docker ps -a --filter "name=charity-mysql" --format "{{.Names}}"
    if ($mysqlExists) {
        docker start charity-mysql
        Write-Host "MySQL started" -ForegroundColor Green
    } else {
        docker run -d `
            --name charity-mysql `
            -e MYSQL_ROOT_PASSWORD=root `
            -e MYSQL_DATABASE=charity_platform `
            -p 3306:3306 `
            mysql:8.0
        Write-Host "MySQL container created and started" -ForegroundColor Green
        Write-Host "Waiting for MySQL to be ready..." -ForegroundColor Yellow
        Start-Sleep -Seconds 10
    }
}

# Start Redis
Write-Host "`n[2/3] Starting Redis..." -ForegroundColor Yellow
$redisRunning = docker ps -a --filter "name=charity-redis" --format "{{.Status}}" | Select-String "Up"
if ($redisRunning) {
    Write-Host "Redis is already running" -ForegroundColor Green
} else {
    $redisExists = docker ps -a --filter "name=charity-redis" --format "{{.Names}}"
    if ($redisExists) {
        docker start charity-redis
        Write-Host "Redis started" -ForegroundColor Green
    } else {
        docker run -d `
            --name charity-redis `
            -p 6379:6379 `
            redis:7-alpine
        Write-Host "Redis container created and started" -ForegroundColor Green
    }
}

# Start RabbitMQ
Write-Host "`n[3/3] Starting RabbitMQ..." -ForegroundColor Yellow
$rabbitmqRunning = docker ps -a --filter "name=charity-rabbitmq" --format "{{.Status}}" | Select-String "Up"
if ($rabbitmqRunning) {
    Write-Host "RabbitMQ is already running" -ForegroundColor Green
} else {
    $rabbitmqExists = docker ps -a --filter "name=charity-rabbitmq" --format "{{.Names}}"
    if ($rabbitmqExists) {
        docker start charity-rabbitmq
        Write-Host "RabbitMQ started" -ForegroundColor Green
    } else {
        docker run -d `
            --name charity-rabbitmq `
            -p 5672:5672 `
            -p 15672:15672 `
            -e RABBITMQ_DEFAULT_USER=guest `
            -e RABBITMQ_DEFAULT_PASS=guest `
            rabbitmq:3-management
        Write-Host "RabbitMQ container created and started" -ForegroundColor Green
        Write-Host "Waiting for RabbitMQ to be ready..." -ForegroundColor Yellow
        Start-Sleep -Seconds 15
        
        Write-Host "Installing RabbitMQ Delayed Message Plugin..." -ForegroundColor Yellow
        docker exec charity-rabbitmq rabbitmq-plugins enable rabbitmq_delayed_message_exchange
        Write-Host "Plugin installed successfully" -ForegroundColor Green
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "All services are running!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "MySQL:     localhost:3306" -ForegroundColor White
Write-Host "Redis:     localhost:6379" -ForegroundColor White
Write-Host "RabbitMQ:  localhost:5672" -ForegroundColor White
Write-Host "RabbitMQ Management UI: http://localhost:15672" -ForegroundColor White
Write-Host "  Username: guest" -ForegroundColor Gray
Write-Host "  Password: guest" -ForegroundColor Gray
Write-Host "========================================" -ForegroundColor Cyan

