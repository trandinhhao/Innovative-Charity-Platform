# Skill Auction Implementation - Tóm tắt

## Tổng quan

Module Skill Auction đã được refactor và implement theo đúng yêu cầu chương 3 với 3 giải pháp chính:
1. **Pessimistic Locking** cho xử lý bid
2. **Redis + Message Queue** để tối ưu hiệu năng
3. **Delayed Message Queue** để finalize auction đúng thời điểm

## Kiến trúc đã triển khai

### 1. Entity Structure

#### AuctionStatus (enum)
- `PENDING`: Chờ bắt đầu
- `ACTIVE`: Đang diễn ra
- `COMPLETED`: Đã kết thúc
- `CANCELLED`: Đã hủy

#### SkillAuction (Entity - Phiên đấu giá)
- `id`: Auction ID
- `startingBid`: Giá khởi điểm
- `currentBid`: Giá hiện tại
- `targetAmount`: Mức mong muốn (optional)
- `startTime`, `endTime`: Thời gian bắt đầu/kết thúc
- `status`: AuctionStatus enum
- `highestBidderId`: ID người đặt giá cao nhất
- Relations: Skill, SkillOwner, Campaign, Bids, Transactions

#### Bid (Entity - Từng lượt đặt giá)
- `id`: Bid ID
- `bidAmount`: Số tiền đặt giá
- `bidTime`: Thời gian đặt giá
- `clientTimestamp`: Timestamp từ client (tie-breaker)
- Relations: SkillAuction, Bidder

#### Transaction (Entity - Giao dịch tài chính)
- `id`: Transaction ID
- `amount`: Số tiền
- `status`: TransactionStatus (PENDING, COMPLETED, FAILED, CANCELLED)
- `description`: Mô tả
- Relations: Winner, SkillAuction, Campaign

### 2. Services Layer

#### BidService (Pessimistic Locking)
**Trách nhiệm**: Xử lý bid với DB lock để tránh race condition

**Quy trình**:
1. Lock SkillAuction với `PESSIMISTIC_WRITE`
2. Validate:
   - Status == ACTIVE
   - Chưa hết hạn (now <= endTime)
   - bidAmount > currentBid
   - (Optional) Không tự outbid chính mình
3. Cập nhật SkillAuction (currentBid, highestBidderId)
4. Tạo Bid record (audit trail)
5. Commit transaction

**Isolation Level**: `REPEATABLE_READ`

#### AuctionStateCacheService (Redis)
**Trách nhiệm**: Quản lý cache auction state trong Redis

**Key Structure**: `auction:{auctionId}`
**Fields**: currentBid, highestBidderId, status, endTime

**Methods**:
- `getAuctionState()`: Lấy state từ Redis
- `updateAuctionState()`: Cập nhật sau khi bid thành công
- `finalizeAuctionState()`: Cập nhật khi finalize
- `evictAuctionState()`: Xóa cache

#### BidProducer (Message Queue)
**Trách nhiệm**: Đẩy bid request vào RabbitMQ queue

**Exchange**: `bid.exchange`
**Queue**: `bid.queue`
**Routing Key**: `bid.routing.key`

#### BidConsumer (Message Queue Worker)
**Trách nhiệm**: Xử lý bid request từ queue

**Quy trình**:
1. Parse message
2. Validate sơ bộ với Redis (filter nhanh):
   - Status != ACTIVE → reject
   - Đã hết hạn → reject
   - bidAmount <= currentBid → reject
3. Xử lý với DB + Pessimistic Lock (xác nhận chính thức)
4. Update Redis nếu thành công
5. (TODO) Publish event để UI update real-time

#### FinalizationService
**Trách nhiệm**: Xử lý kết thúc phiên đấu giá

**Quy trình**:
1. Lock SkillAuction với PESSIMISTIC_WRITE
2. Kiểm tra idempotent (đã finalize chưa?)
3. Xác định người thắng:
   - Tìm bid cao nhất
   - Nếu tie → người đặt sớm hơn (bidTime ASC)
4. Cập nhật status = COMPLETED
5. Tạo Transaction (PENDING status)
6. Update Redis
7. (TODO) Publish notification

#### FinalizationProducer
**Trách nhiệm**: Schedule delayed message cho finalization

**Quy trình**:
1. Tính delay = endTime - now
2. Đẩy message vào delayed exchange với delay
3. Message sẽ được deliver đúng thời điểm endTime

### 3. Configuration

#### RabbitMQConfig
- **Bid Exchange/Queue**: Xử lý bid requests
- **Finalization Exchange/Queue**: Delayed messages cho finalization
- **Message Converter**: Jackson2JsonMessageConverter
- **Concurrent Consumers**: 3 workers (có thể scale lên 10)

#### RedisConfig
- **Connection**: localhost:6379 (có thể config qua env)
- **Serializer**: StringRedisSerializer
- **TTL**: 24 giờ cho auction state cache

#### application.yml
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

rabbitmq:
  host: ${RABBITMQ_HOST:localhost}
  port: ${RABBITMQ_PORT:5672}
  exchange:
    bid: bid.exchange
    finalization: finalization.exchange
  queue:
    bid: bid.queue
    finalization: finalization.queue
```

### 4. API Endpoints

#### POST /api/v1/auctions/{auctionId}/bids
**Request**:
- `bidderId`: Long (query param)
- `bidAmount`: BigDecimal (query param)

**Response**:
```json
{
  "result": "Bid đã được ghi nhận, đang xử lý"
}
```

**Flow**:
1. Validate cơ bản (format, > 0)
2. Tạo BidRequest
3. Đẩy vào queue
4. Trả về ngay (không đợi xử lý)

## Luồng xử lý Bid

### Client → API → Queue → Worker → DB → Redis

1. **Client** gọi `POST /auctions/{id}/bids`
2. **AuctionController** validate và đẩy vào queue
3. **BidConsumer** nhận message:
   - Validate với Redis (filter nhanh)
   - Xử lý với DB + Pessimistic Lock
   - Update Redis
4. **UI** có thể poll hoặc nhận real-time update (TODO)

## Luồng Finalization

### Create Auction → Schedule → Delayed Queue → Worker → Finalize

1. **Tạo SkillAuction** → gọi `FinalizationProducer.scheduleFinalization()`
2. **FinalizationProducer** tính delay và đẩy vào delayed exchange
3. **Delayed Message** được deliver đúng thời điểm endTime
4. **FinalizationConsumer** nhận message và gọi `FinalizationService.finalizeAuction()`
5. **FinalizationService**:
   - Lock SkillAuction
   - Xác định winner
   - Tạo Transaction
   - Update Redis
   - Publish notification

## Điểm nổi bật

### 1. Pessimistic Locking
- Sử dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)` trong repository
- Isolation level: `REPEATABLE_READ`
- Chỉ lock SkillAuction, không lock thêm resource khác (tránh deadlock)

### 2. Redis Cache
- Filter nhanh trước khi vào DB
- Giảm load cho database
- Update sau mỗi bid thành công

### 3. Message Queue
- Decouple API và xử lý bid
- Xử lý song song nhiều bid (3-10 workers)
- Tránh bottleneck khi có nhiều bid cùng lúc

### 4. Delayed Queue
- Finalize đúng thời điểm, không cần polling
- Idempotent: đảm bảo chỉ finalize 1 lần
- Xử lý edge cases (đã finalize, chưa hết hạn, không có bid)

## Files đã tạo/cập nhật

### Mới tạo:
1. `AuctionStatus.java` - Enum
2. `TransactionStatus.java` - Enum
3. `Bid.java` - Entity
4. `Transaction.java` - Entity
5. `BidRepository.java` - Repository
6. `TransactionRepository.java` - Repository
7. `BidService.java` + `BidServiceImpl.java` - Service với pessimistic locking
8. `AuctionStateCacheService.java` + `AuctionStateCacheServiceImpl.java` - Redis cache
9. `BidProducer.java` + `BidProducerImpl.java` - Queue producer
10. `BidConsumer.java` + `BidConsumerImpl.java` - Queue consumer
11. `FinalizationService.java` + `FinalizationServiceImpl.java` - Finalization logic
12. `FinalizationProducer.java` + `FinalizationProducerImpl.java` - Delayed queue producer
13. `FinalizationConsumerImpl.java` - Delayed queue consumer
14. `RabbitMQConfig.java` - RabbitMQ configuration
15. `RedisConfig.java` - Redis configuration
16. `AuctionController.java` - API controller
17. `BidRequest.java` - DTO
18. `BidResult.java` - DTO

### Đã cập nhật:
1. `SkillAuction.java` - Refactor thành auction session
2. `SkillAuctionRepository.java` - Thêm pessimistic lock method
3. `application.yml` - Thêm Redis và RabbitMQ config
4. `pom.xml` - Thêm dependencies (spring-boot-starter-data-redis, spring-boot-starter-amqp)

## Cần làm thêm

1. **Tạo SkillAuctionService**: Service để tạo và quản lý phiên đấu giá
   - `createAuction()`: Tạo auction và schedule finalization
   - `getAuction()`: Lấy thông tin auction
   - `listAuctions()`: List auctions

2. **Refactor SkillService**: Tách logic auction ra khỏi SkillService

3. **Error Handling**: Thêm ErrorCode cho auction (AUCTION_NOT_FOUND, AUCTION_NOT_ACTIVE, etc.)

4. **Real-time Updates**: 
   - Redis Pub/Sub hoặc WebSocket để UI update real-time khi có bid mới
   - Publish event sau mỗi bid thành công

5. **Testing**:
   - Unit test cho BidService (pessimistic locking)
   - Integration test cho race condition
   - Test finalization với delayed queue

6. **Monitoring**:
   - Metrics cho bid processing time
   - Queue depth monitoring
   - Redis cache hit rate

## Lưu ý

### RabbitMQ Delayed Message Plugin
Để sử dụng delayed messages, cần cài đặt RabbitMQ Delayed Message Plugin:
```bash
rabbitmq-plugins enable rabbitmq_delayed_message_exchange
```

### Redis và RabbitMQ Setup
Cần chạy Redis và RabbitMQ servers:
- Redis: `docker run -d -p 6379:6379 redis`
- RabbitMQ: `docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3-management`

### Database Migration
Cần tạo migration script để:
- Tạo bảng `bids`
- Tạo bảng `transactions`
- Cập nhật bảng `skill_auctions` với các field mới

