package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.ChatRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ChatService {
    String chat(ChatRequest request);
    String chatWithImage(MultipartFile file, String message);
}
