package de.mhus.pallaver.model;

import de.mhus.pallaver.ui.ModelOptions;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;

public interface LLType {

    String getTitle();
    String getName();
    String getUrl();

    default boolean equals(LLType type) {
        return getName().equals(type.getName());
    }

    Tokenizer createTekenizer(LLModel model);

    StreamingChatLanguageModel createStreamingChatModel(LLModel model, ModelOptions options);
}
