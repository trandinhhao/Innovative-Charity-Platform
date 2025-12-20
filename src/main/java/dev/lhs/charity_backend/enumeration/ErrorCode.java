package dev.lhs.charity_backend.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    ACCOUNT_INFO_EXISTED(1002, "Account information already exists", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED (1007, "You do not have permission", HttpStatus.FORBIDDEN),

    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1009, "Email is not valid", HttpStatus.BAD_REQUEST),
    INVALID_PHONENUMBER(1010, "Phone number is not valid", HttpStatus.BAD_REQUEST),
    INVALID_PERMISSION(1011, "Permission does not exists", HttpStatus.BAD_REQUEST),
    INVALID_ROLE(1012, "Role does not exists", HttpStatus.BAD_REQUEST),

    CAMPAIGN_NOT_EXISTED(1013, "Campaign not existed", HttpStatus.NOT_FOUND),
    ORGANIZATION_NOT_EXISTED(1014, "Organization not existed", HttpStatus.NOT_FOUND),
    INVALID_BID_PRICE(1015, "Bid price must be greater than current",
            HttpStatus.BAD_REQUEST),
    SKILL_NOT_EXISTED(1016, "Skill not existed", HttpStatus.NOT_FOUND),
    CHALLENGE_NOT_EXISTED(1017, "Challenge not existed", HttpStatus.NOT_FOUND),
    COMMENT_NOT_EXISTED(1018, "Comment not existed", HttpStatus.NOT_FOUND),

    IMAGE_UPLOAD_FAIL(1019, "Upload image fail!", HttpStatus.BAD_REQUEST),
    
    AUCTION_NOT_EXISTED(1020, "Auction not existed", HttpStatus.NOT_FOUND),
    AUCTION_NOT_ACTIVE(1021, "Auction is not active", HttpStatus.BAD_REQUEST),
    AUCTION_EXPIRED(1022, "Auction has expired", HttpStatus.BAD_REQUEST),
    AUCTION_ALREADY_COMPLETED(1023, "Auction already completed", HttpStatus.BAD_REQUEST),
    BID_AMOUNT_TOO_LOW(1024, "Bid amount must be greater than current bid", HttpStatus.BAD_REQUEST),
    BID_SELF_OUTBID(1025, "You are already the highest bidder", HttpStatus.BAD_REQUEST)
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
