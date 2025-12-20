package dev.lhs.charity_backend.dto.payment_payos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayOSApiResponse<T> {
    private Integer error;
    private String message;
    private T data;

    public static <T> PayOSApiResponse<T> success(T data) {
        return new PayOSApiResponse<>(0, "success", data);
    }

    public static <T> PayOSApiResponse<T> success(String message, T data) {
        return new PayOSApiResponse<>(0, message, data);
    }

    public static <T> PayOSApiResponse<T> error(String message) {
        return new PayOSApiResponse<>(-1, message, null);
    }

    public static <T> PayOSApiResponse<T> error(int code, String message) {
        return new PayOSApiResponse<>(code, message, null);
    }
}