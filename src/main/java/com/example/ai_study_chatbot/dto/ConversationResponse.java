package com.example.ai_study_chatbot.dto;

import java.time.LocalDateTime;

public class ConversationResponse {

    private String conversationId;
    private String title;
    private LocalDateTime updatedAt;

    public ConversationResponse() {
    }

    public ConversationResponse(
            String conversationId,
            String title,
            LocalDateTime updatedAt) {

        this.conversationId = conversationId;
        this.title = title;
        this.updatedAt = updatedAt;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}