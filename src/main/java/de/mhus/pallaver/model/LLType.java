package de.mhus.pallaver.model;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

public interface LLType {

    String getTitle();
    String getName();
    String getUrl();

    default boolean equals(LLType type) {
        return getName().equals(type.getName());
    }

    ChatLanguageModel createChatModel(LLModel model);
}
