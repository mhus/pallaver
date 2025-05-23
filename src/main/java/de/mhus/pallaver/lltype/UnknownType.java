package de.mhus.pallaver.lltype;

import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelOptions;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

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
    public TokenCountEstimator createTokenizer(LLModel model) {
        throw new RuntimeException("Unknown type");
    }

    @Override
    public StreamingChatModel createStreamingChatModel(LLModel model, ModelOptions options) {
        throw new RuntimeException("Unknown type");
    }

    @Override
    public boolean supports(LLModel model, String feature) {
        return false;
    }

    @Override
    public ChatModel createChatModel(LLModel model, ModelOptions options) {
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
