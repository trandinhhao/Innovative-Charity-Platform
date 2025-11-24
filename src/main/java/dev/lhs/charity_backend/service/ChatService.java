package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.ChatRequest;
import dev.lhs.charity_backend.dto.response.UserSubmitResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ChatService {
    String chat(ChatRequest request);
    String chatWithImage(MultipartFile file, String message);
    UserSubmitResponse checkSubmitProof(MultipartFile file, String description);
}
