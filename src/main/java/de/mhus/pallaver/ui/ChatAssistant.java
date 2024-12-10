package de.mhus.pallaver.ui;

import dev.langchain4j.service.UserMessage;

public interface ChatAssistant {
    String generate(@UserMessage String userMessage);
}
