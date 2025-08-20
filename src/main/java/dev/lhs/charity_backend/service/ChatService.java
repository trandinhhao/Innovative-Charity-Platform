package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.ChatRequest;

public interface ChatService {
    String chat(ChatRequest request);
}
