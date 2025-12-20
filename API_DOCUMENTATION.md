# API Documentation - Charity Backend

**Base URL**: `http://localhost:8080/api/v1`

**Response Format**: Tất cả API đều trả về format `ApiResponse<T>`:

```json
{
  "code": 1000,
  "message": "Success message (optional)",
  "result": { ... } // Data tùy theo từng API
}
```

---

## 📋 Mục lục

### API Mới Triển Khai (Chi tiết)
1. [Evidence Verification Service](#1-evidence-verification-service)
2. [Skill Auction Service](#2-skill-auction-service)

### API Cũ (Liệt kê)
3. [Authentication](#3-authentication)
4. [User Management](#4-user-management)
5. [Campaign](#5-campaign)
6. [Challenge](#6-challenge)
7. [Skill](#7-skill)
8. [Organization](#8-organization)
9. [Role & Permission](#9-role--permission)
10. [Chat](#10-chat)
11. [Image Upload](#11-image-upload)
12. [Payment (PayOS)](#12-payment-payos)

---

## 1. Evidence Verification Service

### 1.1. Submit Proof (Upload Minh Chứng)

**Endpoint**: `POST /challenges/{challengeId}/submit/{userId}`

**Description**: User upload ảnh minh chứng cho thử thách. Hệ thống sẽ xử lý async và trả về ngay với status PROCESSING.

**Request**:
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`
- **Path Parameters**:
  - `challengeId` (Long): ID của thử thách
  - `userId` (Long): ID của user
- **Form Data**:
  - `file` (MultipartFile): File ảnh minh chứng (jpg, png, jpeg)

**Request Example** (cURL):
```bash
curl -X POST "http://localhost:8080/api/v1/challenges/1/submit/1" \
  -H "Authorization: Bearer {token}" \
  -F "file=@/path/to/proof.jpg"
```

**Response Example** (Status: PROCESSING):
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "id": 1,
    "proofImageUrl": "https://res.cloudinary.com/.../proof.jpg",
    "submitTime": "2024-01-15T10:30:00",
    "message": null,
    "verificationStatus": "PROCESSING",
    "status": 1,
    "confidenceScore": null,
    "analysisDetails": null,
    "rejectionReason": null,
    "processedAt": null,
    "userId": 1,
    "challengeId": 1,
    "isMatch": false
  }
}
```

**Response Example** (Status: APPROVED - sau khi xử lý xong):
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "id": 1,
    "proofImageUrl": "https://res.cloudinary.com/.../proof.jpg",
    "submitTime": "2024-01-15T10:30:00",
    "message": "Minh chứng đã được phê duyệt",
    "verificationStatus": "APPROVED",
    "status": 2,
    "confidenceScore": 0.95,
    "analysisDetails": "{\"objectsDetected\":[\"cây\",\"người\"],\"actionsDetected\":[\"trồng cây\"],\"meetsRequirements\":true}",
    "rejectionReason": null,
    "processedAt": "2024-01-15T10:30:15",
    "userId": 1,
    "challengeId": 1,
    "isMatch": true
  }
}
```

**Response Example** (Status: REJECTED):
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "id": 1,
    "proofImageUrl": "https://res.cloudinary.com/.../proof.jpg",
    "submitTime": "2024-01-15T10:30:00",
    "message": "Minh chứng không đáp ứng yêu cầu",
    "verificationStatus": "REJECTED",
    "status": 3,
    "confidenceScore": 0.85,
    "analysisDetails": "{\"objectsDetected\":[\"cây\"],\"actionsDetected\":[],\"meetsRequirements\":false}",
    "rejectionReason": "Ảnh không có người thực hiện hành động trồng cây",
    "processedAt": "2024-01-15T10:30:12",
    "userId": 1,
    "challengeId": 1,
    "isMatch": false
  }
}
```

**Response Example** (Status: NEEDS_MANUAL_REVIEW):
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "id": 1,
    "proofImageUrl": "https://res.cloudinary.com/.../proof.jpg",
    "submitTime": "2024-01-15T10:30:00",
    "message": "Minh chứng cần được xem xét thủ công",
    "verificationStatus": "NEEDS_MANUAL_REVIEW",
    "status": 4,
    "confidenceScore": 0.75,
    "analysisDetails": "{\"objectsDetected\":[\"cây\",\"người\"],\"actionsDetected\":[\"trồng cây\"],\"meetsRequirements\":true}",
    "rejectionReason": null,
    "processedAt": "2024-01-15T10:30:14",
    "userId": 1,
    "challengeId": 1,
    "isMatch": true
  }
}
```

**VerificationStatus Values**:
- `PENDING` (0): Đang chờ xử lý
- `PROCESSING` (1): Đang xử lý
- `APPROVED` (2): Đã được phê duyệt
- `REJECTED` (3): Đã bị từ chối
- `NEEDS_MANUAL_REVIEW` (4): Cần xem xét thủ công

**Error Responses**:
```json
// Challenge không tồn tại
{
  "code": 1017,
  "message": "Challenge not existed",
  "result": null
}

// User không tồn tại
{
  "code": 1005,
  "message": "User not existed",
  "result": null
}

// Upload ảnh thất bại
{
  "code": 1019,
  "message": "Upload image fail!",
  "result": null
}
```

---

### 1.2. Get Verification Status

**Endpoint**: `GET /challenges/verification/{userChallengeId}`

**Description**: Lấy trạng thái verification mới nhất của minh chứng.

**Request**:
- **Method**: `GET`
- **Path Parameters**:
  - `userChallengeId` (Long): ID của UserChallenge

**Request Example** (cURL):
```bash
curl -X GET "http://localhost:8080/api/v1/challenges/verification/1" \
  -H "Authorization: Bearer {token}"
```

**Response Example** (Tương tự như API Submit Proof):
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "id": 1,
    "proofImageUrl": "https://res.cloudinary.com/.../proof.jpg",
    "submitTime": "2024-01-15T10:30:00",
    "message": "Minh chứng đã được phê duyệt",
    "verificationStatus": "APPROVED",
    "status": 2,
    "confidenceScore": 0.95,
    "analysisDetails": "{...}",
    "rejectionReason": null,
    "processedAt": "2024-01-15T10:30:15",
    "userId": 1,
    "challengeId": 1,
    "isMatch": true
  }
}
```

**Error Response**:
```json
// UserChallenge không tồn tại
{
  "code": 1000,
  "message": "UserChallenge not found with ID: 1",
  "result": null
}
```

---

## 2. Skill Auction Service

### 2.1. Create Auction

**Endpoint**: `POST /auctions`

**Description**: Tạo phiên đấu giá kỹ năng mới. Hệ thống sẽ tự động init Redis cache và schedule finalization.

**Request**:
- **Method**: `POST`
- **Content-Type**: `application/json`
- **Body**:
```json
{
  "skillId": 1,
  "skillOwnerId": 2,
  "campaignId": 3,
  "startingBid": 100000,
  "targetAmount": 500000,
  "startTime": "2024-01-15T10:00:00",
  "endTime": "2024-01-20T18:00:00"
}
```

**Request Fields**:
- `skillId` (Long, required): ID của skill được đấu giá
- `skillOwnerId` (Long, required): ID của user sở hữu skill
- `campaignId` (Long, required): ID của campaign nhận tiền
- `startingBid` (BigDecimal, required): Mức giá khởi điểm
- `targetAmount` (BigDecimal, optional): Mức giá mong muốn
- `startTime` (LocalDateTime, required): Thời gian bắt đầu
- `endTime` (LocalDateTime, required): Thời gian kết thúc

**Request Example** (cURL):
```bash
curl -X POST "http://localhost:8080/api/v1/auctions" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "skillId": 1,
    "skillOwnerId": 2,
    "campaignId": 3,
    "startingBid": 100000,
    "targetAmount": 500000,
    "startTime": "2024-01-15T10:00:00",
    "endTime": "2024-01-20T18:00:00"
  }'
```

**Response Example**:
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "id": 1,
    "skillId": 1,
    "skillName": "Thiết kế Logo",
    "skillOwnerId": 2,
    "skillOwnerName": "Nguyễn Văn A",
    "campaignId": 3,
    "campaignName": "Quyên góp cho trẻ em nghèo",
    "startingBid": 100000,
    "currentBid": 100000,
    "targetAmount": 500000,
    "highestBidderId": null,
    "startTime": "2024-01-15T10:00:00",
    "endTime": "2024-01-20T18:00:00",
    "status": "PENDING",
    "statusCode": 0,
    "createdAt": "2024-01-15T09:00:00",
    "updatedAt": "2024-01-15T09:00:00"
  }
}
```

**Error Responses**:
```json
// Skill không tồn tại
{
  "code": 1016,
  "message": "Skill not existed",
  "result": null
}

// Campaign không tồn tại
{
  "code": 1013,
  "message": "Campaign not existed",
  "result": null
}

// User không tồn tại
{
  "code": 1005,
  "message": "User not existed",
  "result": null
}
```

---

### 2.2. Get Auction

**Endpoint**: `GET /auctions/{auctionId}`

**Description**: Lấy thông tin chi tiết của một phiên đấu giá.

**Request**:
- **Method**: `GET`
- **Path Parameters**:
  - `auctionId` (Long): ID của auction

**Request Example** (cURL):
```bash
curl -X GET "http://localhost:8080/api/v1/auctions/1" \
  -H "Authorization: Bearer {token}"
```

**Response Example** (Auction đang ACTIVE):
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "id": 1,
    "skillId": 1,
    "skillName": "Thiết kế Logo",
    "skillOwnerId": 2,
    "skillOwnerName": "Nguyễn Văn A",
    "campaignId": 3,
    "campaignName": "Quyên góp cho trẻ em nghèo",
    "startingBid": 100000,
    "currentBid": 250000,
    "targetAmount": 500000,
    "highestBidderId": 5,
    "startTime": "2024-01-15T10:00:00",
    "endTime": "2024-01-20T18:00:00",
    "status": "ACTIVE",
    "statusCode": 1,
    "createdAt": "2024-01-15T09:00:00",
    "updatedAt": "2024-01-18T14:30:00"
  }
}
```

**Response Example** (Auction đã COMPLETED):
```json
{
  "code": 1000,
  "message": null,
  "result": {
    "id": 1,
    "skillId": 1,
    "skillName": "Thiết kế Logo",
    "skillOwnerId": 2,
    "skillOwnerName": "Nguyễn Văn A",
    "campaignId": 3,
    "campaignName": "Quyên góp cho trẻ em nghèo",
    "startingBid": 100000,
    "currentBid": 500000,
    "targetAmount": 500000,
    "highestBidderId": 5,
    "startTime": "2024-01-15T10:00:00",
    "endTime": "2024-01-20T18:00:00",
    "status": "COMPLETED",
    "statusCode": 2,
    "createdAt": "2024-01-15T09:00:00",
    "updatedAt": "2024-01-20T18:00:00"
  }
}
```

**AuctionStatus Values**:
- `PENDING` (0): Chờ bắt đầu
- `ACTIVE` (1): Đang diễn ra
- `COMPLETED` (2): Đã kết thúc
- `CANCELLED` (3): Đã hủy

**Error Response**:
```json
// Auction không tồn tại
{
  "code": 1020,
  "message": "Auction not existed",
  "result": null
}
```

---

### 2.3. List All Auctions

**Endpoint**: `GET /auctions`

**Description**: Lấy danh sách tất cả phiên đấu giá.

**Request**:
- **Method**: `GET`

**Request Example** (cURL):
```bash
curl -X GET "http://localhost:8080/api/v1/auctions" \
  -H "Authorization: Bearer {token}"
```

**Response Example**:
```json
{
  "code": 1000,
  "message": null,
  "result": [
    {
      "id": 1,
      "skillId": 1,
      "skillOwnerId": 2,
      "campaignId": 3,
      "startingBid": 100000,
      "currentBid": 250000,
      "targetAmount": 500000,
      "highestBidderId": 5,
      "startTime": "2024-01-15T10:00:00",
      "endTime": "2024-01-20T18:00:00",
      "status": "ACTIVE",
      "statusCode": 1,
      "createdAt": "2024-01-15T09:00:00",
      "updatedAt": "2024-01-18T14:30:00"
    },
    {
      "id": 2,
      "skillId": 2,
      "skillOwnerId": 3,
      "campaignId": 4,
      "startingBid": 200000,
      "currentBid": 200000,
      "targetAmount": 1000000,
      "highestBidderId": null,
      "highestBidderName": null,
      "startTime": "2024-01-16T10:00:00",
      "endTime": "2024-01-25T18:00:00",
      "status": "PENDING",
      "statusCode": 0,
      "createdAt": "2024-01-16T09:00:00",
      "updatedAt": "2024-01-16T09:00:00"
    }
  ]
}
```

---

### 2.4. List Active Auctions

**Endpoint**: `GET /auctions/active`

**Description**: Lấy danh sách các phiên đấu giá đang ACTIVE (có thể đặt giá).

**Request**:
- **Method**: `GET`

**Request Example** (cURL):
```bash
curl -X GET "http://localhost:8080/api/v1/auctions/active" \
  -H "Authorization: Bearer {token}"
```

**Response Example**:
```json
{
  "code": 1000,
  "message": null,
  "result": [
    {
      "id": 1,
      "skillId": 1,
      "skillOwnerId": 2,
      "campaignId": 3,
      "startingBid": 100000,
      "currentBid": 250000,
      "targetAmount": 500000,
      "highestBidderId": 5,
      "startTime": "2024-01-15T10:00:00",
      "endTime": "2024-01-20T18:00:00",
      "status": "ACTIVE",
      "statusCode": 1,
      "createdAt": "2024-01-15T09:00:00",
      "updatedAt": "2024-01-18T14:30:00"
    }
  ]
}
```

---

### 2.5. List Auctions by Campaign

**Endpoint**: `GET /auctions/campaign/{campaignId}`

**Description**: Lấy danh sách các phiên đấu giá theo campaign.

**Request**:
- **Method**: `GET`
- **Path Parameters**:
  - `campaignId` (Long): ID của campaign

**Request Example** (cURL):
```bash
curl -X GET "http://localhost:8080/api/v1/auctions/campaign/3" \
  -H "Authorization: Bearer {token}"
```

**Response Example**: (Tương tự như List All Auctions)

---

### 2.6. Place Bid (Đặt Giá)

**Endpoint**: `POST /auctions/{auctionId}/bids`

**Description**: Đặt giá cho phiên đấu giá. Request được đẩy vào queue và xử lý async. API trả về ngay với message "bid đã được ghi nhận".

**Request**:
- **Method**: `POST`
- **Path Parameters**:
  - `auctionId` (Long): ID của auction
- **Query Parameters**:
  - `bidderId` (Long, required): ID của người đặt giá
  - `bidAmount` (BigDecimal, required): Mức giá đặt

**Request Example** (cURL):
```bash
curl -X POST "http://localhost:8080/api/v1/auctions/1/bids?bidderId=5&bidAmount=300000" \
  -H "Authorization: Bearer {token}"
```

**Response Example** (Success):
```json
{
  "code": 1000,
  "message": null,
  "result": "Bid đã được ghi nhận, đang xử lý"
}
```

**Response Example** (Invalid Amount):
```json
{
  "code": 1000,
  "message": null,
  "result": "Mức giá không hợp lệ"
}
```

**Lưu ý**: 
- Bid được xử lý async qua RabbitMQ queue
- Hệ thống sẽ validate với Redis cache trước, sau đó vào DB với pessimistic lock
- Nếu bid thành công, Redis cache sẽ được update
- Client có thể poll auction status để biết bid có thành công không

**Error Responses** (Từ queue processing - không trả về trực tiếp từ API này):
- `AUCTION_NOT_EXISTED` (1020): Auction không tồn tại
- `AUCTION_NOT_ACTIVE` (1021): Auction không ở trạng thái ACTIVE
- `AUCTION_EXPIRED` (1022): Auction đã hết hạn
- `AUCTION_ALREADY_COMPLETED` (1023): Auction đã kết thúc
- `BID_AMOUNT_TOO_LOW` (1024): Mức giá thấp hơn current bid
- `BID_SELF_OUTBID` (1025): Bạn đã là người đặt giá cao nhất

---

## 3. Authentication

### 3.1. Login
- **Endpoint**: `POST /authentication/token`
- **Description**: Đăng nhập và nhận JWT token

### 3.2. Introspect Token
- **Endpoint**: `POST /authentication/introspect`
- **Description**: Kiểm tra tính hợp lệ của token

### 3.3. Refresh Token
- **Endpoint**: `POST /authentication/refresh`
- **Description**: Làm mới access token

### 3.4. Logout
- **Endpoint**: `DELETE /authentication/logout`
- **Description**: Đăng xuất

---

## 4. User Management

### 4.1. Create User
- **Endpoint**: `POST /users`
- **Description**: Tạo user mới

### 4.2. List Users
- **Endpoint**: `GET /users`
- **Description**: Lấy danh sách tất cả users

### 4.3. Get User
- **Endpoint**: `GET /users/{userId}`
- **Description**: Lấy thông tin user theo ID

### 4.4. Get My Info
- **Endpoint**: `GET /users/my-info`
- **Description**: Lấy thông tin user hiện tại (từ JWT token)

### 4.5. Delete User
- **Endpoint**: `DELETE /users/{userId}`
- **Description**: Xóa user

### 4.6. Update User
- **Endpoint**: `PUT /users/{userId}`
- **Description**: Cập nhật thông tin user

---

## 5. Campaign

### 5.1. Create Campaign
- **Endpoint**: `POST /campaigns`
- **Description**: Tạo campaign mới

### 5.2. List Campaigns
- **Endpoint**: `GET /campaigns`
- **Description**: Lấy danh sách tất cả campaigns

### 5.3. Get Campaign
- **Endpoint**: `GET /campaigns/{campId}`
- **Description**: Lấy thông tin campaign theo ID

### 5.4. Delete Campaign
- **Endpoint**: `DELETE /campaigns/{campId}`
- **Description**: Xóa campaign

### 5.5. Update Campaign
- **Endpoint**: `PUT /campaigns/{campId}`
- **Description**: Cập nhật campaign

### 5.6. Add Comment
- **Endpoint**: `POST /campaigns/{campId}/comments`
- **Description**: Thêm comment vào campaign

### 5.7. Get Comments
- **Endpoint**: `GET /campaigns/{campId}/comments`
- **Description**: Lấy danh sách comments của campaign

### 5.8. Delete Comment
- **Endpoint**: `DELETE /campaigns/comments/{commentId}`
- **Description**: Xóa comment

---

## 6. Challenge

### 6.1. Create Challenge
- **Endpoint**: `POST /challenges/{userId}`
- **Description**: Tạo challenge mới

### 6.2. List Challenges
- **Endpoint**: `GET /challenges`
- **Description**: Lấy danh sách tất cả challenges

### 6.3. Get Challenge
- **Endpoint**: `GET /challenges/{challengeId}`
- **Description**: Lấy thông tin challenge theo ID

### 6.4. Delete Challenge
- **Endpoint**: `DELETE /challenges/{challengeId}`
- **Description**: Xóa challenge

### 6.5. Submit Proof
- **Endpoint**: `POST /challenges/{challengeId}/submit/{userId}`
- **Description**: Upload minh chứng (xem chi tiết ở phần 1.1)

### 6.6. Get Verification Status
- **Endpoint**: `GET /challenges/verification/{userChallengeId}`
- **Description**: Lấy trạng thái verification (xem chi tiết ở phần 1.2)

---

## 7. Skill

### 7.1. Create Skill
- **Endpoint**: `POST /skills/{userId}`
- **Description**: Tạo skill mới

### 7.2. List Skills
- **Endpoint**: `GET /skills`
- **Description**: Lấy danh sách tất cả skills

### 7.3. Get Skill
- **Endpoint**: `GET /skills/{skillId}`
- **Description**: Lấy thông tin skill theo ID

### 7.4. Delete Skill
- **Endpoint**: `DELETE /skills/{skillId}`
- **Description**: Xóa skill

### 7.5. Create Skill Auction (Old API)
- **Endpoint**: `POST /skills/auction/{userId}/{skillId}`
- **Description**: Tạo auction cho skill (API cũ, nên dùng `/auctions` mới)

---

## 8. Organization

### 8.1. Create Organization
- **Endpoint**: `POST /organizations`
- **Description**: Tạo organization mới

### 8.2. List Organizations
- **Endpoint**: `GET /organizations`
- **Description**: Lấy danh sách tất cả organizations

### 8.3. Get Organization
- **Endpoint**: `GET /organizations/{orgId}`
- **Description**: Lấy thông tin organization theo ID

### 8.4. Delete Organization
- **Endpoint**: `DELETE /organizations/{orgId}`
- **Description**: Xóa organization

### 8.5. Update Organization
- **Endpoint**: `PUT /organizations/{orgId}`
- **Description**: Cập nhật organization

---

## 9. Role & Permission

### 9.1. Create Role
- **Endpoint**: `POST /roles`
- **Description**: Tạo role mới

### 9.2. List Roles
- **Endpoint**: `GET /roles`
- **Description**: Lấy danh sách tất cả roles

### 9.3. Delete Role
- **Endpoint**: `DELETE /roles/{role}`
- **Description**: Xóa role

### 9.4. Create Permission
- **Endpoint**: `POST /permissions`
- **Description**: Tạo permission mới

### 9.5. List Permissions
- **Endpoint**: `GET /permissions`
- **Description**: Lấy danh sách tất cả permissions

### 9.6. Delete Permission
- **Endpoint**: `DELETE /permissions/{permission}`
- **Description**: Xóa permission

---

## 10. Chat

### 10.1. Chat (Text Only)
- **Endpoint**: `POST /chat/chat`
- **Description**: Chat với AI (chỉ text)

### 10.2. Chat with Image
- **Endpoint**: `POST /chat/chat-with-image`
- **Description**: Chat với AI (text + image)

---

## 11. Image Upload

### 11.1. Upload Image
- **Endpoint**: `POST /images/upload`
- **Description**: Upload ảnh lên Cloudinary

---

## 12. Payment (PayOS)

### 12.1. Create Payment Link
- **Endpoint**: `POST /orders/create`
- **Description**: Tạo payment link với PayOS

### 12.2. Get Order
- **Endpoint**: `GET /orders/{orderId}`
- **Description**: Lấy thông tin order

### 12.3. Update Order
- **Endpoint**: `PUT /orders/{orderId}`
- **Description**: Cập nhật order

### 12.4. Confirm Webhook
- **Endpoint**: `POST /orders/confirm-webhook`
- **Description**: Xác nhận webhook từ PayOS

### 12.5. PayOS Transfer Handler
- **Endpoint**: `POST /payments/payos_transfer_handler`
- **Description**: Xử lý transfer từ PayOS

---

## 🔐 Authentication

Tất cả API (trừ Authentication endpoints) đều yêu cầu JWT token trong header:

```
Authorization: Bearer {token}
```

---

## 📝 Notes

1. **Async Processing**: 
   - Evidence Verification và Bid Processing được xử lý async
   - Client nên poll status để biết kết quả

2. **Error Codes**: 
   - Xem chi tiết trong `ErrorCode` enum
   - Code 1000 = Success
   - Code khác = Error

3. **Date Format**: 
   - Tất cả datetime dùng format ISO 8601: `yyyy-MM-ddTHH:mm:ss`

4. **BigDecimal**: 
   - Tất cả số tiền dùng BigDecimal (không dùng Double/Float)

5. **File Upload**: 
   - Chỉ hỗ trợ image: jpg, jpeg, png
   - Max size: Tùy cấu hình Cloudinary

---

## 🧪 Testing

### Postman Collection

Có thể import các API vào Postman để test:

1. Tạo Environment với variables:
   - `base_url`: `http://localhost:8080/api/v1`
   - `token`: JWT token sau khi login

2. Test flow:
   - Login → Get token
   - Create Campaign
   - Create Challenge
   - Submit Proof → Poll verification status
   - Create Auction
   - Place Bid → Poll auction status

---

**Last Updated**: 2024-01-15

