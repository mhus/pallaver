package de.mhus.pallaver.ui;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.util.List;

public interface Assistant {
    TokenStream generate(@UserMessage List<ChatMessage> userMessage);
}
