
package com.example.ai_study_chatbot.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.example.ai_study_chatbot.entity.Conversation;
import com.example.ai_study_chatbot.entity.Message;
import com.example.ai_study_chatbot.entity.Role;
import com.example.ai_study_chatbot.repository.ConversationRepository;
import com.example.ai_study_chatbot.repository.MessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    public ChatService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository) {

        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    // CREATE NEW CONVERSATION
    public String createConversation() {

        String id = UUID.randomUUID().toString();

        Conversation conversation = new Conversation(id);

        conversationRepository.save(conversation);

        return id;
    }

    // GET MESSAGES FROM CONVERSATION
    @Transactional(readOnly = true)
    public List<Message> getMessages(String conversationId) {

        Conversation conversation = conversationRepository
                .findByConversationId(conversationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Conversation not found: " + conversationId));

        return List.copyOf(conversation.getMessages());
    }

    // GET ALL CONVERSATIONS
    @Transactional(readOnly = true)
    public List<Conversation> getAllConversations() {

        List<Conversation> conversations =
                conversationRepository.findAllByOrderByUpdatedAtDesc();

        for (Conversation conversation : conversations) {
            conversation.getMessages().size();
        }

        return conversations;
    }

    // SAVE USER MESSAGE
    @Transactional
    public void saveUserMessage(
            String conversationId, String content) {

        saveMessage(conversationId, content, Role.USER);
    }

    // SAVE ASSISTANT MESSAGE
    @Transactional
    public void saveAssistantMessage(
            String conversationId, String content) {

        saveMessage(conversationId, content, Role.ASSISTANT);
    }

    // SAVE MESSAGE IN MYSQL
    private void saveMessage(
            String conversationId,
            String content,
            Role role) {

        Conversation conversation = conversationRepository
                .findByConversationId(conversationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Conversation not found: " + conversationId));

        Message message = new Message(role, content);

        conversation.addMessage(message);

        conversationRepository.save(conversation);
    }

    // GENERATE AI RESPONSE WITH CONVERSATION MEMORY
    public String generateResponse(String conversationId) {

        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException(
                    "Conversation ID cannot be empty");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Gemini API key is missing");
        }

        try {

            String url =
                    "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            // BUILD GEMINI REQUEST
            var requestBody = objectMapper.createObjectNode();

            var contents = requestBody.putArray("contents");

            // FETCH SAVED CONVERSATION MESSAGES
            List<Message> messages = getMessages(conversationId);

            if (messages.isEmpty()) {
                throw new IllegalStateException(
                        "Conversation has no messages");
            }

            // ADD CONVERSATION HISTORY TO GEMINI REQUEST
            for (Message message : messages) {

                var content = contents.addObject();

                if (message.getRole() == Role.USER) {
                    content.put("role", "user");
                } else {
                    content.put("role", "model");
                }

                var parts = content.putArray("parts");

                parts.addObject().put(
                        "text",
                        message.getContent()
                );
            }

            HttpEntity<String> request = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody),
                    headers);

            // AUTOMATIC RETRY FOR GEMINI 503 ERRORS
            String response = null;

            for (int attempt = 1; attempt <= 3; attempt++) {

                try {

                    response = restTemplate.postForObject(
                            url,
                            request,
                            String.class);

                    break;

                } catch (HttpServerErrorException e) {

                    if (e.getStatusCode().value() != 503
                            || attempt == 3) {
                        throw e;
                    }

                    long delay = attempt * 2000L;

                    System.out.println(
                            "Gemini is busy. Retrying in "
                            + (delay / 1000)
                            + " seconds...");

                    Thread.sleep(delay);
                }
            }

            if (response == null || response.isBlank()) {
                throw new IllegalStateException(
                        "Gemini returned an empty response");
            }

            // READ GEMINI RESPONSE
            JsonNode root = objectMapper.readTree(response);

            JsonNode candidates = root.path("candidates");

            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new IllegalStateException(
                        "Gemini returned no response candidates");
            }

            StringBuilder answer = new StringBuilder();

            JsonNode responseParts = candidates.get(0)
                    .path("content")
                    .path("parts");

            for (JsonNode part : responseParts) {

                String text = part.path("text").asText("");

                if (!text.isBlank()) {

                    if (answer.length() > 0) {
                        answer.append("\n");
                    }

                    answer.append(text);
                }
            }

            if (answer.length() == 0) {
                throw new IllegalStateException(
                        "No text found in Gemini response");
            }

            return answer.toString();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Gemini request interrupted", e);

        } catch (RestClientResponseException e) {

            throw new IllegalStateException(
                    "Gemini API error: "
                    + e.getStatusCode()
                    + " - "
                    + e.getResponseBodyAsString(),
                    e);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to generate Gemini response: "
                    + e.getMessage(),
                    e);
        }
    }
}
