package com.example.ai_study_chatbot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ai_study_chatbot.entity.Conversation;

public interface ConversationRepository
        extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByConversationId(
            String conversationId
    );

    List<Conversation> findAllByOrderByUpdatedAtDesc();
}