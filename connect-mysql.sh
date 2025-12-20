#!/bin/bash
# Script để kết nối MySQL nhanh (Bash)

echo "=== Kết nối MySQL Database ==="
echo ""

# Kiểm tra container có đang chạy không
if ! docker ps --format "{{.Names}}" | grep -q "charity-mysql"; then
    echo "❌ Container MySQL chưa chạy!"
    echo "Đang start container..."
    docker-compose up -d mysql
    sleep 5
fi

# Đọc password từ .env file (nếu có)
ENV_FILE=".env"
PASSWORD="root"  # Default

if [ -f "$ENV_FILE" ]; then
    PASSWORD=$(grep "DBMS_PASSWORD=" "$ENV_FILE" | cut -d '=' -f2 | tr -d ' ')
fi

echo "📊 Thông tin kết nối:"
echo "   Host: localhost"
echo "   Port: 3307"
echo "   Database: charity_platform"
echo "   Username: root"
echo ""

echo "🔌 Đang kết nối..."
echo ""

# Kết nối vào MySQL
docker exec -it charity-mysql mysql -u root -p"$PASSWORD" charity_platform

