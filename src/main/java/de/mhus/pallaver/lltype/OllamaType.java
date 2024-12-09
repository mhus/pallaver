package de.mhus.pallaver.lltype;


import de.mhus.commons.tools.MString;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.LLType;
import de.mhus.pallaver.ui.ModelOptions;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenizer;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import lombok.Getter;

import java.time.Duration;

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
    public StreamingChatLanguageModel createStreamingChatModel(LLModel model, ModelOptions options) {
        var builder = OllamaStreamingChatModel.builder();
        builder.baseUrl(url);
        builder.modelName(modelName);
        builder.temperature(options.getTemperature());
        if (MString.isSet(options.getFormat()))
            builder.format(options.getFormat());
        if (options.getSeed() != null)
            builder.seed(options.getSeed());
        if (options.getTimeoutInSeconds() != null)
            builder.timeout(Duration.ofSeconds(options.getTimeoutInSeconds()));
        return builder.build();
    }

    @Override
    public Tokenizer createTekenizer(LLModel model) {
        return new HuggingFaceTokenizer();
    }

}
