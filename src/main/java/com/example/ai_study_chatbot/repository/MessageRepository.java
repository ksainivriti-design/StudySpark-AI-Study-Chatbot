package com.example.ai_study_chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ai_study_chatbot.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}