package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.request.ChatRequest;
import dev.lhs.charity_backend.dto.response.UserSubmitResponse;
import dev.lhs.charity_backend.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final JdbcChatMemoryRepository jdbcChatMemoryRepository;

    public ChatServiceImpl (ChatClient.Builder builder, JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        this.jdbcChatMemoryRepository = jdbcChatMemoryRepository;

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(30)
                .build();

        chatClient = builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()).build();
    }

    @Override
    public String chat(ChatRequest request) {
        // moi lan chat can co 1 id rieng
        String conversationId = SecurityContextHolder.getContext().getAuthentication().getName();
        SystemMessage systemMessage = new SystemMessage("""
                You are LHS.AI
                You must response with a formal voice
                """);

        UserMessage userMessage = new UserMessage(request.message());

        Prompt prompt = new Prompt(systemMessage, userMessage);

        return chatClient
                .prompt(prompt)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    @Override
    public String chatWithImage(MultipartFile file, String message) {
        Media media = Media.builder()
                .mimeType(MimeTypeUtils.parseMimeType(file.getContentType()))
                .data(file.getResource())
                .build();

        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0D) // ra kqua on dinh nhat, k dc sang tao
                .build();

        return chatClient.prompt()
                .options(chatOptions)
                .system("You are LHS.AI")
                .user(promptUserSpec -> promptUserSpec.media(media).text(message))
                .call()
                .content();
    }

    @Override
    public UserSubmitResponse checkSubmitProof(MultipartFile file, String description) {
        Media media = Media.builder()
                .mimeType(MimeTypeUtils.parseMimeType(file.getContentType()))
                .data(file.getResource())
                .build();

        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0D) // ra kqua on dinh nhat, k dc sang tao
                .build();

        String systemPrompt =
        """
        Bạn là LHS.AI - Một AI hỗ trợ kiểm tra xem người dùng có thực hiện đúng yêu cầu không.
        Bạn phải phân tích mô tả tôi đã truyền vào, xem bức ảnh người dùng truyền vào có đúng với yêu cầu không.
        Bạn phải kiểm tra thật kĩ, từng chi tiết trong yêu cầu, tuyệt đối không bỏ qua bất kì yêu cầu nào.
        Bạn phải chắc chắn tất cả những gì có trong yêu cầu phải được xuất hiện trong ảnh.
        Bạn phải trả về CHÍNH XÁC dạng JSON response như yêu cầu dưới đây:
        {
            "message": "Một đoạn nhận xét (< 20 từ), mô tả về người dùng đã thực hiện ổn chưa",
            "isMatch": "trả ra chính xác "YES" hoặc "NO" xem có khớp không? không được trả ra cái nào khác"
        }
        Không được trả ra bất kì cái gì khác ngoài JSON này!
        """;

        return chatClient.prompt()
                .options(chatOptions)
                .system(systemPrompt)
                .user(promptUserSpec -> promptUserSpec.media(media).text(description))
                .call()
                .entity(new ParameterizedTypeReference<UserSubmitResponse>() {});
    }
}
