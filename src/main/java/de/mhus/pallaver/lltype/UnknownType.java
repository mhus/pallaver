package de.mhus.pallaver.lltype;

import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.LLType;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;

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
    public String getUrl() {
        return "";
    }

    @Override
    public ChatLanguageModel createChatModel(LLModel model) {
        throw new RuntimeException("Unknown type");
    }

    @Override
    public Tokenizer createTekenizer(LLModel model) {
        throw new RuntimeException("Unknown type");
    }

    @Override
    public StreamingChatLanguageModel createStreamingChatModel(LLModel model) {
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
