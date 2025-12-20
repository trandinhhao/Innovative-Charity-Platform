#!/bin/bash
# Không dùng set -e vì một số lệnh có thể fail (như download plugin)
set +e

# Download plugin nếu chưa có
PLUGIN_DIR="/opt/rabbitmq/plugins"
cd "$PLUGIN_DIR"

# Kiểm tra xem đã có plugin nào chưa
if ls rabbitmq_delayed_message_exchange-*.ez 1> /dev/null 2>&1; then
    echo "Plugin already exists, skipping download..."
else
    echo "Downloading rabbitmq_delayed_message_exchange plugin..."
    
    # Thử download các version khác nhau (từ mới nhất đến cũ nhất)
    DOWNLOADED=false
    
    for version in "3.11.1" "3.10.2" "3.9.0"; do
        PLUGIN_FILE="rabbitmq_delayed_message_exchange-${version}.ez"
        URL="https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/${version}/${PLUGIN_FILE}"
        
        echo "Trying version ${version}..."
        if wget -q "$URL" -O "$PLUGIN_FILE" 2>/dev/null || curl -L -f "$URL" -o "$PLUGIN_FILE" 2>/dev/null; then
            echo "Plugin downloaded successfully: $PLUGIN_FILE"
            DOWNLOADED=true
            break
        fi
    done
    
    if [ "$DOWNLOADED" = false ]; then
        echo "Warning: Could not download plugin from any version. Delayed message exchange may not work."
        echo "You may need to manually download and place the plugin in $PLUGIN_DIR"
    fi
fi

# Start RabbitMQ in background
docker-entrypoint.sh rabbitmq-server -detached

# Wait for RabbitMQ to be ready
echo "Waiting for RabbitMQ to start..."
for i in {1..30}; do
    if rabbitmqctl ping 2>/dev/null; then
        echo "RabbitMQ is ready!"
        break
    fi
    sleep 2
done

# Enable delayed message exchange plugin
echo "Enabling rabbitmq_delayed_message_exchange plugin..."
rabbitmq-plugins enable rabbitmq_delayed_message_exchange || echo "Warning: Failed to enable plugin (may already be enabled)"

# Stop RabbitMQ
rabbitmqctl stop

# Start RabbitMQ in foreground
set -e  # Bật lại error handling cho phần cuối
exec docker-entrypoint.sh rabbitmq-server

