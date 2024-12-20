package de.mhus.pallaver.lltype;

import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelOptions;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;

import java.util.List;

public class UnknownType implements LLType {
    private final String value;

    public UnknownType(String value) {
        this.value = value;
    }

    @Override
    public String getTitle() {
        return "??? " + value;
    }

    @Override
    public String getName() {
        return value;
    }

    @Override
    public List<LLModel> getDefaultModels() {
        return List.of();
    }

    @Override
    public Tokenizer createTokenizer(LLModel model) {
        throw new RuntimeException("Unknown type");
    }

    @Override
    public StreamingChatLanguageModel createStreamingChatModel(LLModel model, ModelOptions options) {
        throw new RuntimeException("Unknown type");
    }

    @Override
    public boolean supports(LLModel model, String feature) {
        return false;
    }

    @Override
    public ChatLanguageModel createChatModel(LLModel model, ModelOptions options) {
        throw new RuntimeException("Unknown type");
    }

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
