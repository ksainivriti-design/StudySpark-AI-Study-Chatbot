package com.example.ai_study_chatbot.dto;

import java.time.LocalDateTime;

import com.example.ai_study_chatbot.entity.Role;

public class MessageResponse {

    private Role role;
    private String content;
    private LocalDateTime createdAt;

    public MessageResponse() {
    }

    public MessageResponse(
            Role role,
            String content,
            LocalDateTime createdAt) {

        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Role getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}