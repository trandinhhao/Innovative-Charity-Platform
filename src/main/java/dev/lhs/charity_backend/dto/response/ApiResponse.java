package dev.lhs.charity_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // tra ve nhung field k bi null
public class ApiResponse<T> {
    @Builder.Default
    private int code = 1000;// @builder thi nhung cai khai bao nay se bi mat neu k .default
    private String message;
    private T result;
}
