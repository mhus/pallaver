package de.mhus.pallaver.lltype;

import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelOptions;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

import java.util.List;

public interface LLType {

    String getTitle();
    String getName();

    default boolean equals(LLType type) {
        return getName().equals(type.getName());
    }

    List<LLModel> getDefaultModels();

    TokenCountEstimator createTokenizer(LLModel model);

    StreamingChatModel createStreamingChatModel(LLModel model, ModelOptions options);

    boolean supports(LLModel model, String feature);

    ChatModel createChatModel(LLModel model, ModelOptions options);

    int getMaxTokens(LLModel model);
}
