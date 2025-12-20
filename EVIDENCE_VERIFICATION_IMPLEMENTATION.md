# Evidence Verification Service - Tóm tắt Implementation

## Tổng quan

Module "Evidence Verification Service" đã được triển khai theo đúng yêu cầu chương 2 với pipeline 7 bước để xử lý minh chứng từ thiện.

## Kiến trúc đã triển khai

### 1. Enum và Entity

#### VerificationStatus (enum)
- `PENDING`: Đang chờ xử lý
- `PROCESSING`: Đang xử lý
- `APPROVED`: Đã được phê duyệt
- `REJECTED`: Đã bị từ chối
- `NEEDS_MANUAL_REVIEW`: Cần xem xét thủ công

#### UserChallenge Entity (đã cập nhật)
Các field mới:
- `verificationStatus`: VerificationStatus enum
- `confidenceScore`: Double (0.0 - 1.0)
- `analysisDetails`: String (JSON chứa phân tích chi tiết)
- `rejectionReason`: String (nullable)
- `processedAt`: LocalDateTime

### 2. Services Layer

#### ImagePreprocessingService
**Trách nhiệm**: Bước 4 - Tiền xử lý ảnh
- Resize ảnh nếu quá lớn (max 1920x1920)
- Validate format và size
- Trích metadata (width, height, format, size)

#### EvidenceAnalysisService
**Trách nhiệm**: Bước 5 - Tích hợp Spring AI
- Xây dựng prompt từ Challenge requirements
- Gửi ảnh + prompt đến AI API
- Nhận và parse JSON response thành AIAnalysisResult

**Prompt Structure**:
- System prompt: Định nghĩa vai trò AI (chuyên gia đánh giá minh chứng)
- User prompt: Challenge requirements + JSON output format

#### EvaluationService
**Trách nhiệm**: Bước 6 - Đánh giá và quyết định status

**Logic đánh giá**:
1. `confidenceScore < 0.70` → `NEEDS_MANUAL_REVIEW`
2. `meetsRequirements = false` → `REJECTED` (kèm rejectionReason)
3. `meetsRequirements = true && confidenceScore >= 0.90` → `APPROVED`
4. `meetsRequirements = true && 0.70 <= confidenceScore < 0.90` → `NEEDS_MANUAL_REVIEW`

#### EvidenceVerificationService
**Trách nhiệm**: Orchestrate toàn bộ pipeline 7 bước

**Pipeline**:
1. User thực hiện thử thách (đã có)
2. User chụp ảnh (đã có)
3. Upload ảnh lên Cloudinary → lưu URL
4. Tiền xử lý ảnh (resize/nén/trích metadata)
5. Gửi ảnh + requirements đến AI
6. So khớp kết quả AI → tính confidenceScore → quyết định status
7. Lưu kết quả vào DB và trả về

**Methods**:
- `verifyEvidenceAsync()`: Xử lý async (trả về ngay với status PROCESSING)
- `verifyEvidence()`: Xử lý sync (đợi kết quả)
- `getVerificationStatus()`: Lấy trạng thái verification

### 3. DTOs

#### AIAnalysisResult
POJO để map JSON response từ AI:
- `objectsDetected`: List<String>
- `actionsDetected`: List<String>
- `context`: String
- `meetsRequirements`: Boolean
- `confidenceScore`: Double
- `detailedAnalysis`: Map<String, RequirementAnalysis>
- `fraudIndicators`: List<String>
- `rejectionReason`: String

#### VerificationResult
Kết quả đánh giá sau khi xử lý:
- `status`: VerificationStatus
- `message`: String
- `confidenceScore`: Double
- `meetsRequirements`: Boolean
- `rejectionReason`: String
- `analysisDetails`: String (JSON)

#### UserChallengeResponse (đã cập nhật)
Thêm các field:
- `verificationStatus`
- `confidenceScore`
- `analysisDetails`
- `rejectionReason`
- `processedAt`

### 4. Configuration

#### AsyncConfig
Cấu hình thread pool cho async processing:
- Core pool size: 10
- Max pool size: 50
- Queue capacity: 200
- Hỗ trợ xử lý song song 200-300 requests

#### application.yml
Cấu hình Spring AI cho evidence verification:
- Temperature: 0.0 (ổn định, không sáng tạo)
- Max tokens: 2000
- Timeout: 30s

### 5. Controller và Service Updates

#### ChallengeController
- `POST /challenges/{challengeId}/submit/{userId}`: Submit proof (đã refactor)
- `GET /challenges/verification/{userChallengeId}`: Lấy trạng thái verification

#### ChallengeService
- `submitProof()`: Sử dụng EvidenceVerificationService
- `getVerificationStatus()`: Lấy trạng thái verification

## Cách sử dụng

### 1. Submit Proof (Upload minh chứng)

```http
POST /api/v1/challenges/{challengeId}/submit/{userId}
Content-Type: multipart/form-data

file: [image file]
```

**Response**:
```json
{
  "result": {
    "id": 1,
    "proofImageUrl": "https://cloudinary.com/...",
    "verificationStatus": "PROCESSING",
    "status": 1,
    "isMatch": false,
    "confidenceScore": null,
    ...
  }
}
```

### 2. Check Verification Status

```http
GET /api/v1/challenges/verification/{userChallengeId}
```

**Response**:
```json
{
  "result": {
    "id": 1,
    "verificationStatus": "APPROVED",
    "confidenceScore": 0.95,
    "isMatch": true,
    "analysisDetails": "{...}",
    "rejectionReason": null,
    ...
  }
}
```

## Lưu ý quan trọng

### Async Processing
- Khi submit proof, hệ thống trả về ngay với status `PROCESSING`
- Client nên poll status endpoint để lấy kết quả cuối cùng
- Hoặc sử dụng WebSocket/SSE để nhận notification (cần implement thêm)

### Error Handling
- Nếu có lỗi trong quá trình xử lý, status sẽ được set thành `REJECTED`
- `rejectionReason` sẽ chứa thông tin lỗi

### Performance
- Mục tiêu: < 5 giây/ảnh trung bình, tối đa ~15 giây
- Thread pool được cấu hình để xử lý song song nhiều request
- Image preprocessing giúp giảm kích thước và tăng tốc độ xử lý

### Mở rộng trong tương lai
- Hỗ trợ video (trích keyframe)
- Kết hợp metadata (EXIF, GPS) vào prompt
- Cải thiện prompt template với file .txt riêng
- Thêm monitoring và logging chi tiết hơn

## Files đã tạo/cập nhật

### Mới tạo:
1. `VerificationStatus.java` - Enum
2. `AIAnalysisResult.java` - DTO
3. `VerificationResult.java` - DTO
4. `ImagePreprocessingService.java` - Interface
5. `ImagePreprocessingServiceImpl.java` - Implementation
6. `EvidenceAnalysisService.java` - Interface
7. `EvidenceAnalysisServiceImpl.java` - Implementation
8. `EvaluationService.java` - Interface
9. `EvaluationServiceImpl.java` - Implementation
10. `EvidenceVerificationService.java` - Interface
11. `EvidenceVerificationServiceImpl.java` - Implementation
12. `AsyncConfig.java` - Configuration

### Đã cập nhật:
1. `UserChallenge.java` - Entity (thêm fields)
2. `UserChallengeResponse.java` - DTO (thêm fields)
3. `ChallengeService.java` - Interface (thêm method)
4. `ChallengeServiceImpl.java` - Implementation (refactor)
5. `ChallengeController.java` - Controller (thêm endpoint)
6. `application.yml` - Configuration (thêm AI config)

## Testing

Để test module này:

1. Tạo một Challenge với description rõ ràng
2. Submit proof với ảnh phù hợp
3. Kiểm tra status ban đầu là PROCESSING
4. Poll status endpoint để xem kết quả cuối cùng
5. Verify confidenceScore và verificationStatus

## Cần làm thêm (nếu cần)

1. **Prompt Template File**: Tách prompt thành file .txt riêng để dễ chỉnh sửa
2. **WebSocket/SSE**: Thêm real-time notification khi verification hoàn thành
3. **Monitoring**: Thêm metrics cho latency, confidence distribution
4. **Retry Logic**: Thêm retry cho AI calls nếu fail
5. **Caching**: Cache prompt template nếu cần
6. **Validation**: Thêm validation cho image format/size trước khi upload

