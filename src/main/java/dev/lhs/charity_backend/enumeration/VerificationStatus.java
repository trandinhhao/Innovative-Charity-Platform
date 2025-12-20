package dev.lhs.charity_backend.enumeration;

import lombok.Getter;

@Getter
public enum VerificationStatus {
    PENDING(0, "Đang chờ xử lý"),
    PROCESSING(1, "Đang xử lý"),
    APPROVED(2, "Đã được phê duyệt"),
    REJECTED(3, "Đã bị từ chối"),
    NEEDS_MANUAL_REVIEW(4, "Cần xem xét thủ công");

    private final int code;
    private final String description;

    VerificationStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static VerificationStatus fromCode(int code) {
        for (VerificationStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return PENDING;
    }
}

