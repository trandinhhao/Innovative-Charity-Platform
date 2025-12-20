package dev.lhs.charity_backend.enumeration;

import lombok.Getter;

@Getter
public enum AuctionStatus {
    PENDING(0, "Chờ bắt đầu"),
    ACTIVE(1, "Đang diễn ra"),
    COMPLETED(2, "Đã kết thúc"),
    CANCELLED(3, "Đã hủy");

    private final int code;
    private final String description;

    AuctionStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AuctionStatus fromCode(int code) {
        for (AuctionStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return PENDING;
    }
}

