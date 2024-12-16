package de.mhus.pallaver.lltype;

import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelOptions;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;

import java.util.List;

public interface LLType {

    String getTitle();
    String getName();

    default boolean equals(LLType type) {
        return getName().equals(type.getName());
    }

    List<LLModel> getDefaultModels();

    Tokenizer createTokenizer(LLModel model);

    StreamingChatLanguageModel createStreamingChatModel(LLModel model, ModelOptions options);

    boolean supports(LLModel model, String feature);

    ChatLanguageModel createChatModel(LLModel model, ModelOptions options);
}
