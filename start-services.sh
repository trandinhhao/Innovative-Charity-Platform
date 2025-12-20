#!/bin/bash

# Bash script to start all required services

echo "Starting Charity Backend Services..."

# Check if Docker is running
if ! docker ps > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker first."
    exit 1
fi

# Start MySQL
echo ""
echo "[1/3] Starting MySQL..."
if docker ps --filter "name=charity-mysql" --format "{{.Status}}" | grep -q "Up"; then
    echo "MySQL is already running"
else
    if docker ps -a --filter "name=charity-mysql" --format "{{.Names}}" | grep -q "charity-mysql"; then
        docker start charity-mysql
        echo "MySQL started"
    else
        docker run -d \
            --name charity-mysql \
            -e MYSQL_ROOT_PASSWORD=root \
            -e MYSQL_DATABASE=charity_platform \
            -p 3306:3306 \
            mysql:8.0
        echo "MySQL container created and started"
        echo "Waiting for MySQL to be ready..."
        sleep 10
    fi
fi

# Start Redis
echo ""
echo "[2/3] Starting Redis..."
if docker ps --filter "name=charity-redis" --format "{{.Status}}" | grep -q "Up"; then
    echo "Redis is already running"
else
    if docker ps -a --filter "name=charity-redis" --format "{{.Names}}" | grep -q "charity-redis"; then
        docker start charity-redis
        echo "Redis started"
    else
        docker run -d \
            --name charity-redis \
            -p 6379:6379 \
            redis:7-alpine
        echo "Redis container created and started"
    fi
fi

# Start RabbitMQ
echo ""
echo "[3/3] Starting RabbitMQ..."
if docker ps --filter "name=charity-rabbitmq" --format "{{.Status}}" | grep -q "Up"; then
    echo "RabbitMQ is already running"
else
    if docker ps -a --filter "name=charity-rabbitmq" --format "{{.Names}}" | grep -q "charity-rabbitmq"; then
        docker start charity-rabbitmq
        echo "RabbitMQ started"
    else
        docker run -d \
            --name charity-rabbitmq \
            -p 5672:5672 \
            -p 15672:15672 \
            -e RABBITMQ_DEFAULT_USER=guest \
            -e RABBITMQ_DEFAULT_PASS=guest \
            rabbitmq:3-management
        echo "RabbitMQ container created and started"
        echo "Waiting for RabbitMQ to be ready..."
        sleep 15
        
        echo "Installing RabbitMQ Delayed Message Plugin..."
        docker exec charity-rabbitmq rabbitmq-plugins enable rabbitmq_delayed_message_exchange
        echo "Plugin installed successfully"
    fi
fi

echo ""
echo "========================================"
echo "All services are running!"
echo "========================================"
echo "MySQL:     localhost:3306"
echo "Redis:     localhost:6379"
echo "RabbitMQ:  localhost:5672"
echo "RabbitMQ Management UI: http://localhost:15672"
echo "  Username: guest"
echo "  Password: guest"
echo "========================================"

