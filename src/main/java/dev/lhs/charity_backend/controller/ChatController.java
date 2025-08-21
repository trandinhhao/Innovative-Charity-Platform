package dev.lhs.charity_backend.controller;

import dev.lhs.charity_backend.dto.request.ChatRequest;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import dev.lhs.charity_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    ApiResponse<String> chat (@RequestBody ChatRequest request) {
        return ApiResponse.<String>builder()
                .result(chatService.chat(request))
                .build();
    }

    @PostMapping("/chat-with-image")
    ApiResponse<String> chatWithImage(@RequestParam("file") MultipartFile file,
                                      @RequestParam("message") String message) {
        return ApiResponse.<String>builder()
                .result(chatService.chatWithImage(file, message))
                .build();
    }
}
