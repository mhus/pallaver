package de.mhus.pallaver.lltype;


import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.LLType;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.onnx.BertTokenizer;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenizer;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import lombok.Getter;

public class OllamaType implements LLType {
    @Getter
    private final String name;
    @Getter
    private final String url;
    @Getter
    private final String title;
    private final String modelName;

    public OllamaType(String modelName, String url) {
        this.modelName = modelName;
        this.name = "ollama:" + modelName;
        this.title = "Ollama " + modelName;
        this.url = url;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public ChatLanguageModel createChatModel(LLModel model) {
        return OllamaChatModel.builder()
                .baseUrl(url)
                .modelName(modelName)
                .temperature(model.getTemperature()).build();
    }

    @Override
    public StreamingChatLanguageModel createStreamingChatModel(LLModel model) {
        return OllamaStreamingChatModel.builder()
                .baseUrl(url)
                .modelName(modelName)
                .temperature(model.getTemperature()).build();
    }

    @Override
    public Tokenizer createTekenizer(LLModel model) {
        return new HuggingFaceTokenizer();
    }

}
