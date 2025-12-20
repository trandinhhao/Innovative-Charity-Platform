# Skill Auction - Flow Hoàn Chỉnh

## Tổng quan Flow

### 1. Tạo Phiên Đấu Giá (Create Auction)

**API**: `POST /api/v1/auctions`

**Request Body**:
```json
{
  "skillId": 1,
  "skillOwnerId": 2,
  "campaignId": 3,
  "startingBid": 100000,
  "targetAmount": 500000,
  "startTime": "2024-01-01T10:00:00",
  "endTime": "2024-01-01T18:00:00"
}
```

**Flow**:
1. `AuctionController.createAuction()` nhận request
2. `SkillAuctionService.createAuction()`:
   - Validate entities (Skill, User, Campaign)
   - Validate time (startTime >= now, endTime > startTime)
   - Xác định status (PENDING nếu startTime > now, ACTIVE nếu startTime <= now)
   - Tạo SkillAuction entity
   - Lưu vào DB
   - **Initialize Redis cache** với startingBid, endTime, status
   - **Schedule finalization** với delayed message
3. Trả về SkillAuctionResponse

**Kết quả**:
- SkillAuction được tạo với status PENDING hoặc ACTIVE
- Redis cache được init: `auction:{id}` với currentBid, status, endTime
- Delayed message được schedule để finalize đúng endTime

---

### 2. Đặt Giá (Place Bid)

**API**: `POST /api/v1/auctions/{auctionId}/bids?bidderId={userId}&bidAmount={amount}`

**Flow**:
1. `AuctionController.placeBid()` nhận request
2. Validate cơ bản (bidAmount > 0)
3. Tạo BidRequest với clientTimestamp
4. **Đẩy vào RabbitMQ queue** (BidProducer)
5. Trả về ngay: "Bid đã được ghi nhận, đang xử lý"

**Worker xử lý (BidConsumer)**:
1. Nhận message từ queue
2. **Validate sơ bộ với Redis** (filter nhanh):
   - Status != ACTIVE → reject
   - Đã hết hạn (now > endTime) → reject
   - bidAmount <= currentBid → reject
3. **Xử lý với DB + Pessimistic Lock**:
   - Lock SkillAuction với PESSIMISTIC_WRITE
   - Validate lại (status, endTime, bidAmount)
   - Cập nhật SkillAuction (currentBid, highestBidderId)
   - Tạo Bid record (audit trail)
   - Commit transaction
4. **Update Redis cache** nếu thành công
5. (TODO) Publish event để UI update real-time

**Kết quả**:
- Bid được xử lý an toàn với pessimistic locking
- Không có race condition
- Redis cache được update
- Bid record được lưu vào DB

---

### 3. Kết Thúc Phiên Đấu Giá (Finalization)

**Trigger**: Delayed message được deliver đúng thời điểm endTime

**Flow**:
1. **FinalizationConsumer** nhận message từ delayed queue
2. Gọi `FinalizationService.finalizeAuction()`
3. **Lock SkillAuction** với PESSIMISTIC_WRITE
4. **Kiểm tra idempotent**:
   - Nếu status == COMPLETED → return (đã finalize rồi)
   - Nếu status != ACTIVE → return
   - Nếu now < endTime → return (chưa hết hạn)
5. **Xác định người thắng**:
   - Tìm bid cao nhất (ORDER BY bidAmount DESC, bidTime ASC)
   - Nếu không có bid → winnerId = null
6. **Cập nhật status = COMPLETED**
7. **Tạo Transaction** (nếu có winner):
   - amount = finalBid
   - status = PENDING
   - winner, skillAuction, campaign
8. **Update Redis cache** với status COMPLETED
9. (TODO) Publish notification

**Kết quả**:
- Auction status = COMPLETED
- Transaction được tạo (nếu có winner)
- Redis cache được update
- Idempotent: chỉ finalize 1 lần

---

## API Endpoints Đầy Đủ

### Auction Management

1. **POST /api/v1/auctions**
   - Tạo phiên đấu giá mới
   - Body: SkillAuctionCreationRequest

2. **GET /api/v1/auctions/{auctionId}**
   - Lấy thông tin phiên đấu giá

3. **GET /api/v1/auctions**
   - List tất cả phiên đấu giá

4. **GET /api/v1/auctions/active**
   - List phiên đấu giá đang ACTIVE

5. **GET /api/v1/auctions/campaign/{campaignId}**
   - List phiên đấu giá theo campaign

### Bid

6. **POST /api/v1/auctions/{auctionId}/bids**
   - Đặt giá cho phiên đấu giá
   - Query params: bidderId, bidAmount
   - Trả về ngay: "Bid đã được ghi nhận, đang xử lý"

---

## Database Schema

### skill_auctions
- id (PK)
- starting_bid
- current_bid
- target_amount
- start_time
- end_time
- status (enum string)
- status_code (int)
- highest_bidder_id
- skill_id (FK)
- skill_owner_id (FK)
- campaign_id (FK)
- created_at
- updated_at

### bids
- id (PK)
- bid_amount
- bid_time
- client_timestamp
- skill_auction_id (FK)
- user_id (FK - bidder)

### transactions
- id (PK)
- amount
- status (enum string)
- description
- created_at
- winner_id (FK)
- skill_auction_id (FK)
- campaign_id (FK)

---

## Redis Cache Structure

**Key**: `auction:{auctionId}`

**Hash Fields**:
- `currentBid`: String (BigDecimal)
- `highestBidderId`: String (Long, nullable)
- `status`: String (AuctionStatus enum)
- `endTime`: String (ISO LocalDateTime)

**TTL**: 24 giờ

---

## RabbitMQ Configuration

### Bid Queue
- **Exchange**: `bid.exchange` (Topic)
- **Queue**: `bid.queue`
- **Routing Key**: `bid.routing.key`
- **Consumers**: 3-10 workers

### Finalization Queue (Delayed)
- **Exchange**: `finalization.exchange` (Topic, Delayed)
- **Queue**: `finalization.queue`
- **Routing Key**: `finalization.routing.key`
- **Delay**: Tính từ endTime - now

---

## Error Codes

- `AUCTION_NOT_EXISTED` (1020): Auction không tồn tại
- `AUCTION_NOT_ACTIVE` (1021): Auction không đang ACTIVE
- `AUCTION_EXPIRED` (1022): Auction đã hết hạn
- `AUCTION_ALREADY_COMPLETED` (1023): Auction đã kết thúc
- `BID_AMOUNT_TOO_LOW` (1024): Mức giá quá thấp
- `BID_SELF_OUTBID` (1025): Bạn đang là người đặt giá cao nhất

---

## Testing Scenarios

### 1. Race Condition Test
- Nhiều user bid cùng lúc trên cùng 1 auction
- Verify: Chỉ 1 bid thành công, không có lost update

### 2. Finalization Test
- Tạo auction với endTime trong tương lai
- Verify: Delayed message được schedule
- Đợi đến endTime
- Verify: Auction được finalize đúng thời điểm

### 3. Idempotent Finalization
- Gửi finalization message 2 lần
- Verify: Chỉ finalize 1 lần

### 4. Redis Cache Test
- Tạo auction → verify cache được init
- Bid thành công → verify cache được update
- Finalize → verify cache được update

### 5. Queue Processing Test
- Gửi nhiều bid requests cùng lúc
- Verify: Tất cả được xử lý, không mất message

---

## Setup Requirements

### 1. RabbitMQ Delayed Message Plugin
```bash
rabbitmq-plugins enable rabbitmq_delayed_message_exchange
```

### 2. Redis
```bash
docker run -d -p 6379:6379 redis
```

### 3. RabbitMQ
```bash
docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 4. Environment Variables
```yaml
REDIS_HOST=localhost
REDIS_PORT=6379
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

---

## Flow Diagram

```
Create Auction
    ↓
Initialize Redis Cache
    ↓
Schedule Finalization (Delayed Message)
    ↓
[Wait for bids...]
    ↓
Place Bid (API)
    ↓
Queue (RabbitMQ)
    ↓
BidConsumer (Worker)
    ↓
Validate với Redis (filter nhanh)
    ↓
DB + Pessimistic Lock (xác nhận)
    ↓
Update Redis Cache
    ↓
[More bids...]
    ↓
EndTime reached
    ↓
Delayed Message Delivered
    ↓
FinalizationConsumer
    ↓
FinalizationService
    ↓
Lock + Determine Winner
    ↓
Create Transaction
    ↓
Update Redis Cache
    ↓
Complete!
```

---

## Notes

1. **Pessimistic Locking**: Chỉ lock SkillAuction, không lock thêm resource khác (tránh deadlock)

2. **Redis Filter**: Filter nhanh trước khi vào DB, giảm load database

3. **Queue Decoupling**: API không đợi xử lý bid, trả về ngay

4. **Delayed Queue**: Finalize đúng thời điểm, không cần polling

5. **Idempotent**: Finalization chỉ chạy 1 lần, dù message bị deliver nhiều lần

6. **Audit Trail**: Tất cả bid được lưu vào bảng Bid

7. **Transaction**: Được tạo khi finalize, status PENDING (payment flow xử lý sau)

