# Hướng dẫn Setup và Chạy Ứng dụng

## 📋 Tổng quan

Ứng dụng cần các service sau để chạy:
1. **MySQL Database** - Lưu trữ dữ liệu
2. **Redis** - Cache cho auction state
3. **RabbitMQ** - Message queue cho bid processing và finalization
4. **Environment Variables** - Cấu hình API keys và connection strings

---

## 🗄️ 1. Setup MySQL Database

### Cách 1: Dùng Docker (Khuyến nghị)

```bash
docker run -d \
  --name charity-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=charity_platform \
  -p 3306:3306 \
  mysql:8.0
```

### Cách 2: Cài đặt trực tiếp

1. Download và cài MySQL từ [mysql.com](https://dev.mysql.com/downloads/mysql/)
2. Tạo database:
```sql
CREATE DATABASE charity_platform;
```

### Kiểm tra kết nối

```bash
mysql -u root -p -h localhost -P 3306
```

---

## 🔴 2. Setup Redis

### Cách 1: Dùng Docker (Khuyến nghị)

```bash
docker run -d \
  --name charity-redis \
  -p 6379:6379 \
  redis:7-alpine
```

### Cách 2: Cài đặt trực tiếp (Windows)

1. Download từ [redis.io](https://redis.io/download)
2. Hoặc dùng WSL2:
```bash
wsl
sudo apt-get update
sudo apt-get install redis-server
redis-server
```

### Kiểm tra Redis

```bash
# Với Docker
docker exec -it charity-redis redis-cli ping
# Kết quả: PONG

# Hoặc từ máy local
redis-cli ping
```

---

## 🐰 3. Setup RabbitMQ

### Cách 1: Dùng Docker (Khuyến nghị)

```bash
docker run -d \
  --name charity-rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3-management
```

**Management UI**: http://localhost:15672
- Username: `guest`
- Password: `guest`

### Cài đặt RabbitMQ Delayed Message Plugin

**Quan trọng**: Plugin này cần thiết cho finalization delayed messages.

```bash
# Vào container
docker exec -it charity-rabbitmq bash

# Download plugin
cd /opt/rabbitmq/plugins
wget https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/3.12.0/rabbitmq_delayed_message_exchange-3.12.0.ez

# Enable plugin
rabbitmq-plugins enable rabbitmq_delayed_message_exchange

# Exit container
exit

# Restart container
docker restart charity-rabbitmq
```

### Kiểm tra RabbitMQ

1. Truy cập Management UI: http://localhost:15672
2. Login với `guest/guest`
3. Vào tab **Plugins** → Kiểm tra `rabbitmq_delayed_message_exchange` đã enabled

---

## 🔐 4. Setup Environment Variables

Tạo file `.env` trong thư mục root của project (hoặc set trong IDE/OS):

### Windows (PowerShell)

```powershell
# Database
$env:DBMS_CONNECTION = "jdbc:mysql://localhost:3306/charity_platform"
$env:DBMS_USERNAME = "root"
$env:DBMS_PASSWORD = "root"

# Redis
$env:REDIS_HOST = "localhost"
$env:REDIS_PORT = "6379"
$env:REDIS_PASSWORD = ""

# RabbitMQ
$env:RABBITMQ_HOST = "localhost"
$env:RABBITMQ_PORT = "5672"
$env:RABBITMQ_USERNAME = "guest"
$env:RABBITMQ_PASSWORD = "guest"

# Spring AI (OpenRouter)
$env:GEMINI_KEY = "your-openrouter-api-key-here"

# JWT
$env:SIGNER_KEY = "your-jwt-secret-key-here-min-32-characters"

# PayOS (nếu dùng payment)
$env:PAYOS_CLIENT_ID = "your-payos-client-id"
$env:PAYOS_API_KEY = "your-payos-api-key"
$env:PAYOS_CHECKSUM_KEY = "your-payos-checksum-key"
```

### Linux/Mac

```bash
# Tạo file .env
cat > .env << EOF
DBMS_CONNECTION=jdbc:mysql://localhost:3306/charity_platform
DBMS_USERNAME=root
DBMS_PASSWORD=root

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

GEMINI_KEY=your-openrouter-api-key-here
SIGNER_KEY=your-jwt-secret-key-here-min-32-characters

PAYOS_CLIENT_ID=your-payos-client-id
PAYOS_API_KEY=your-payos-api-key
PAYOS_CHECKSUM_KEY=your-payos-checksum-key
EOF

# Load vào shell
export $(cat .env | xargs)
```

### Hoặc set trong IDE (IntelliJ IDEA)

1. Run → Edit Configurations
2. Chọn Application
3. Environment variables → Thêm các biến trên

---

## 🔑 5. Lấy API Keys

### OpenRouter API Key (cho Spring AI)

1. Đăng ký tại [openrouter.ai](https://openrouter.ai/)
2. Tạo API key
3. Set vào `GEMINI_KEY`

**Lưu ý**: OpenRouter hỗ trợ nhiều model, code đang dùng `openai/gpt-4o-mini` (có thể đổi trong `application.yml`)

### JWT Secret Key

Tạo một chuỗi ngẫu nhiên ít nhất 32 ký tự:

```bash
# Linux/Mac
openssl rand -base64 32

# Hoặc dùng online generator
```

---

## 🚀 6. Chạy Ứng dụng

### Bước 1: Kiểm tra các service đang chạy

```bash
# Kiểm tra MySQL
docker ps | grep mysql
# hoặc
mysql -u root -p -h localhost -P 3306

# Kiểm tra Redis
docker ps | grep redis
# hoặc
redis-cli ping

# Kiểm tra RabbitMQ
docker ps | grep rabbitmq
# hoặc truy cập http://localhost:15672
```

### Bước 2: Build project

```bash
mvn clean install -DskipTests
```

### Bước 3: Chạy ứng dụng

```bash
# Cách 1: Dùng Maven
mvn spring-boot:run

# Cách 2: Dùng IDE
# Run file: CharityBackendApplication.java

# Cách 3: Dùng JAR
java -jar target/charity_backend-0.0.1-SNAPSHOT.jar
```

### Bước 4: Kiểm tra ứng dụng

1. **Health check**: http://localhost:8080/api/v1/health (nếu có)
2. **API Base URL**: http://localhost:8080/api/v1
3. **Logs**: Kiểm tra console để xem có lỗi không

---

## ✅ 7. Kiểm tra Setup

### Checklist

- [ ] MySQL đang chạy và có thể kết nối
- [ ] Redis đang chạy và trả về PONG
- [ ] RabbitMQ đang chạy và Management UI accessible
- [ ] RabbitMQ Delayed Message Plugin đã enabled
- [ ] Tất cả environment variables đã set
- [ ] OpenRouter API key hợp lệ
- [ ] JWT secret key đã set (ít nhất 32 ký tự)
- [ ] Application compile thành công
- [ ] Application start thành công không có lỗi

### Test kết nối từ code

Sau khi start ứng dụng, kiểm tra logs:

```
✅ Redis connection: OK
✅ RabbitMQ connection: OK
✅ Database connection: OK
```

Nếu có lỗi, xem phần Troubleshooting bên dưới.

---

## 🔧 8. Troubleshooting

### Lỗi: Cannot connect to MySQL

```
Error: Communications link failure
```

**Giải pháp**:
1. Kiểm tra MySQL đang chạy: `docker ps | grep mysql`
2. Kiểm tra port 3306: `netstat -an | grep 3306`
3. Kiểm tra username/password trong environment variables
4. Kiểm tra database đã được tạo chưa

### Lỗi: Cannot connect to Redis

```
Error: Unable to connect to Redis
```

**Giải pháp**:
1. Kiểm tra Redis đang chạy: `docker ps | grep redis`
2. Kiểm tra port 6379: `netstat -an | grep 6379`
3. Test connection: `redis-cli ping`
4. Nếu dùng password, set `REDIS_PASSWORD` trong env

### Lỗi: Cannot connect to RabbitMQ

```
Error: Connection refused
```

**Giải pháp**:
1. Kiểm tra RabbitMQ đang chạy: `docker ps | grep rabbitmq`
2. Kiểm tra ports 5672 và 15672
3. Truy cập Management UI: http://localhost:15672
4. Kiểm tra username/password

### Lỗi: Delayed message không hoạt động

```
Warning: Delayed message plugin not available
```

**Giải pháp**:
1. Vào RabbitMQ Management UI → Plugins
2. Enable `rabbitmq_delayed_message_exchange`
3. Restart RabbitMQ container
4. Kiểm tra lại trong UI

### Lỗi: Spring AI API key invalid

```
Error: 401 Unauthorized
```

**Giải pháp**:
1. Kiểm tra `GEMINI_KEY` đã set đúng chưa
2. Kiểm tra API key còn valid không
3. Kiểm tra có đủ credit trong OpenRouter account không

### Lỗi: Application không start

**Giải pháp**:
1. Kiểm tra logs chi tiết: `mvn spring-boot:run -X`
2. Kiểm tra port 8080 đã bị chiếm chưa: `netstat -an | grep 8080`
3. Đổi port trong `application.yml` nếu cần
4. Kiểm tra tất cả dependencies đã được download: `mvn dependency:resolve`

---

## 📝 9. Quick Start Script (Windows)

Tạo file `start-services.ps1`:

```powershell
# Start MySQL
docker start charity-mysql 2>$null
if ($LASTEXITCODE -ne 0) {
    docker run -d --name charity-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=charity_platform -p 3306:3306 mysql:8.0
}

# Start Redis
docker start charity-redis 2>$null
if ($LASTEXITCODE -ne 0) {
    docker run -d --name charity-redis -p 6379:6379 redis:7-alpine
}

# Start RabbitMQ
docker start charity-rabbitmq 2>$null
if ($LASTEXITCODE -ne 0) {
    docker run -d --name charity-rabbitmq -p 5672:5672 -p 15672:15672 -e RABBITMQ_DEFAULT_USER=guest -e RABBITMQ_DEFAULT_PASS=guest rabbitmq:3-management
    Write-Host "Waiting for RabbitMQ to start..."
    Start-Sleep -Seconds 10
    Write-Host "Installing delayed message plugin..."
    docker exec charity-rabbitmq rabbitmq-plugins enable rabbitmq_delayed_message_exchange
}

Write-Host "All services started!"
Write-Host "MySQL: localhost:3306"
Write-Host "Redis: localhost:6379"
Write-Host "RabbitMQ: localhost:5672 (Management: http://localhost:15672)"
```

Chạy: `.\start-services.ps1`

---

## 📝 10. Quick Start Script (Linux/Mac)

Tạo file `start-services.sh`:

```bash
#!/bin/bash

# Start MySQL
docker start charity-mysql 2>/dev/null || \
docker run -d --name charity-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=charity_platform \
  -p 3306:3306 \
  mysql:8.0

# Start Redis
docker start charity-redis 2>/dev/null || \
docker run -d --name charity-redis \
  -p 6379:6379 \
  redis:7-alpine

# Start RabbitMQ
docker start charity-rabbitmq 2>/dev/null || \
docker run -d --name charity-rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3-management

# Wait for RabbitMQ
echo "Waiting for RabbitMQ to start..."
sleep 10

# Install delayed message plugin
docker exec charity-rabbitmq rabbitmq-plugins enable rabbitmq_delayed_message_exchange

echo "All services started!"
echo "MySQL: localhost:3306"
echo "Redis: localhost:6379"
echo "RabbitMQ: localhost:5672 (Management: http://localhost:15672)"
```

Chạy: `chmod +x start-services.sh && ./start-services.sh`

---

## 🎯 11. Test API sau khi chạy

### Test Evidence Verification

```bash
# 1. Tạo challenge
POST http://localhost:8080/api/v1/challenges
# Body: { ... }

# 2. Submit proof (upload image)
POST http://localhost:8080/api/v1/challenges/{challengeId}/submit/{userId}
# Form-data: file=image.jpg

# 3. Check verification status
GET http://localhost:8080/api/v1/challenges/verification/{userChallengeId}
```

### Test Skill Auction

```bash
# 1. Tạo auction
POST http://localhost:8080/api/v1/auctions
# Body: { ... }

# 2. Place bid
POST http://localhost:8080/api/v1/auctions/{auctionId}/bids?bidderId=1&bidAmount=100

# 3. Get auction info
GET http://localhost:8080/api/v1/auctions/{auctionId}
```

---

## 📚 Tài liệu tham khảo

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Redis Documentation](https://redis.io/documentation)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [RabbitMQ Delayed Message Plugin](https://github.com/rabbitmq/rabbitmq-delayed-message-exchange)
- [OpenRouter API](https://openrouter.ai/docs)

---

## 💡 Tips

1. **Dùng Docker Compose**: Có thể tạo `docker-compose.yml` để quản lý tất cả services cùng lúc
2. **Environment Variables**: Dùng `.env` file và load vào IDE để dễ quản lý
3. **Logs**: Enable debug logs trong `application.yml` để debug dễ hơn
4. **Database Migration**: Nếu dùng Flyway/Liquibase, cần setup migration scripts
5. **Monitoring**: Có thể dùng Spring Boot Actuator để monitor health

---

Chúc bạn setup thành công! 🎉

