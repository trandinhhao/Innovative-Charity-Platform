# Fix RabbitMQ Delayed Message Exchange Plugin

## Vấn đề
RabbitMQ container cứ sập liên tục vì không tìm thấy plugin `rabbitmq_delayed_message_exchange`.

## Giải pháp
Đã tạo Dockerfile và entrypoint script để tự động download và enable plugin.

## Các bước thực hiện

### 1. Dừng và xóa container cũ
```bash
docker-compose down
docker rm -f charity-rabbitmq
# (Tùy chọn) Xóa volume nếu cần reset hoàn toàn:
# docker volume rm charity_backend_rabbitmq_data
```

### 2. Build lại image RabbitMQ
```bash
docker-compose build rabbitmq
```

**Lưu ý**: Build sẽ nhanh hơn vì chỉ cài wget/curl, không download plugin ở build time.

### 3. Chạy lại services
```bash
docker-compose up -d
```

**Lưu ý**: Lần đầu chạy, container sẽ tự động download plugin khi start. Có thể mất vài phút.

### 4. Kiểm tra plugin đã được enable
```bash
docker exec charity-rabbitmq rabbitmq-plugins list
```

Bạn sẽ thấy `rabbitmq_delayed_message_exchange` trong danh sách với status `[E*]` (enabled).

### 5. Kiểm tra RabbitMQ Management UI
Truy cập: http://localhost:15672
- Username: (từ .env file - RABBITMQ_USERNAME)
- Password: (từ .env file - RABBITMQ_PASSWORD)

Vào tab **Exchanges**, bạn sẽ thấy type `x-delayed-message` có sẵn.

## Cấu trúc files đã tạo

1. **Dockerfile.rabbitmq**: Build image với plugin đã download
2. **docker-entrypoint-rabbitmq.sh**: Script tự động enable plugin khi container start
3. **docker-compose.yml**: Đã cập nhật để dùng build thay vì image trực tiếp

## Lưu ý

- Plugin sẽ được enable tự động khi container start lần đầu
- Nếu vẫn gặp lỗi, kiểm tra logs:
  ```bash
  docker logs charity-rabbitmq
  ```

## Alternative: Nếu không muốn dùng delayed message exchange

Nếu bạn muốn dùng cách khác (Spring Scheduler hoặc Redis Sorted Set), có thể:
1. Xóa Dockerfile.rabbitmq và docker-entrypoint-rabbitmq.sh
2. Sửa docker-compose.yml để dùng image trực tiếp:
   ```yaml
   rabbitmq:
     image: rabbitmq:3-management
     # ... rest of config
   ```
3. Sửa FinalizationProducer để dùng Spring Scheduler thay vì delayed queue

