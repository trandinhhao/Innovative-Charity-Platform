package dev.lhs.charity_backend.enumeration;

import lombok.Getter;

@Getter
public enum TransactionStatus {
    PENDING(0, "Chờ xử lý"),
    COMPLETED(1, "Đã hoàn thành"),
    FAILED(2, "Thất bại"),
    CANCELLED(3, "Đã hủy");

    private final int code;
    private final String description;

    TransactionStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TransactionStatus fromCode(int code) {
        for (TransactionStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return PENDING;
    }
}

