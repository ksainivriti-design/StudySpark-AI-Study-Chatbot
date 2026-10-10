package com.example.ai_study_chatbot.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ai_study_chatbot.dto.ChatRequest;
import com.example.ai_study_chatbot.dto.ChatResponse;
import com.example.ai_study_chatbot.dto.ConversationResponse;
import com.example.ai_study_chatbot.dto.MessageResponse;
import com.example.ai_study_chatbot.entity.Conversation;
import com.example.ai_study_chatbot.entity.Message;
import com.example.ai_study_chatbot.service.ChatService;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // -----------------------------------------
    // CREATE NEW CONVERSATION
    // -----------------------------------------

    @PostMapping("/conversation")
    public Map<String, String> createConversation() {

        String conversationId =
                chatService.createConversation();

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "conversationId",
                conversationId
        );

        return response;
    }


    // -----------------------------------------
    // GET MESSAGES FROM ONE CONVERSATION
    // -----------------------------------------

    @GetMapping("/conversation/{conversationId}")
    public List<MessageResponse> getMessages(
            @PathVariable String conversationId) {

        List<Message> messages =
                chatService.getMessages(conversationId);

        return messages.stream()
                .map(message ->
                        new MessageResponse(
                                message.getRole(),
                                message.getContent(),
                                message.getCreatedAt()
                        )
                )
                .collect(Collectors.toList());
    }


    // -----------------------------------------
    // GET ALL CONVERSATIONS
    // USED FOR SIDEBAR HISTORY
    // -----------------------------------------

    @GetMapping("/conversations")
    public List<ConversationResponse> getConversations() {

        List<Conversation> conversations =
                chatService.getAllConversations();

        return conversations.stream()
                .map(conversation -> {

                    String title =
                            "New Study Chat";

                    if (!conversation
                            .getMessages()
                            .isEmpty()) {

                        Message firstMessage =
                                conversation
                                        .getMessages()
                                        .get(0);

                        if (firstMessage
                                .getContent() != null &&
                                !firstMessage
                                        .getContent()
                                        .isBlank()) {

                            title =
                                    firstMessage
                                            .getContent()
                                            .trim();

                            if (title.length() > 30) {

                                title =
                                        title.substring(0, 30)
                                                + "...";
                            }
                        }
                    }

                    return new ConversationResponse(
                            conversation.getConversationId(),
                            title,
                            conversation.getUpdatedAt()
                    );
                })
                .collect(Collectors.toList());
    }


    // -----------------------------------------
    // SEND MESSAGE
    // -----------------------------------------

    @PostMapping
    public ChatResponse chat(
            @RequestBody ChatRequest request) {

        String conversationId =
                request.getConversationId();

        // Create conversation if one
        // does not already exist
        if (conversationId == null ||
                conversationId.isBlank()) {

            conversationId  =
                    chatService.createConversation();
        }

        String question =
                request.getMessage();

        // Save student's question
        chatService.saveUserMessage(
                conversationId,
                question
        );

        String answer =
        chatService.generateResponse(
                conversationId
        );

        // Save AI answer
        chatService.saveAssistantMessage(
                conversationId,
                answer
        );

        return new ChatResponse(
                conversationId,
                answer
        );
    }
}