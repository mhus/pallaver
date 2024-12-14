package de.mhus.pallaver.chat;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.util.List;

public interface StreamChatAssistant {
    TokenStream generate(@UserMessage List<ChatMessage> userMessage);
}
