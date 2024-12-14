package de.mhus.pallaver.chat;

import dev.langchain4j.service.UserMessage;

public interface ChatAssistant {
    String generate(@UserMessage String userMessage);
}
