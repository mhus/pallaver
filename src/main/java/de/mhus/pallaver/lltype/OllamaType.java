package de.mhus.pallaver.lltype;


import de.mhus.commons.tools.MString;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelOptions;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenizer;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaModels;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class OllamaType implements LLType {

    @Getter
    private final String name = "ollama";
    @Getter
    @Value("${pallaver.ollama.url:http://localhost:11434}")
    private String defaultUrl;
    @Value("${pallaver.ollama.apiKey:}")
    private String defaultApiKey;
    @Getter
    private final String title = "Ollama";
    private final List<String> supportedFeatures = List.of(LLMFeatures.STREAM, LLMFeatures.TOOLS);

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
        builder.baseUrl(model.getUrl());
        builder.modelName(model.getModel());
        builder.temperature(options.getTemperature());
        if (MString.isSet(options.getFormat()))
            builder.format(options.getFormat());
        if (options.getSeed() != null)
            builder.seed(options.getSeed());
        if (options.getTimeoutInSeconds() != null)
            builder.timeout(Duration.ofSeconds(options.getTimeoutInSeconds()));

        if (options.isLogging())
            builder.logRequests(true).logResponses(true);

        return builder.build();
    }

    @Override
    public boolean supports(LLModel model, String feature) {
        return supportedFeatures.contains(feature);
    }

    @Override
    public ChatLanguageModel createChatModel(LLModel model, ModelOptions options) {
        var builder = OllamaChatModel.builder();
        builder.baseUrl(model.getUrl());
        builder.modelName(model.getModel());
        builder.temperature(options.getTemperature());
        if (MString.isSet(options.getFormat()))
            builder.format(options.getFormat());
        if (options.getSeed() != null)
            builder.seed(options.getSeed());
        if (options.getTimeoutInSeconds() != null)
            builder.timeout(Duration.ofSeconds(options.getTimeoutInSeconds()));

        if (options.isLogging())
            builder.logRequests(true).logResponses(true);

        return builder.build();
    }

    @Override
    public List<LLModel> getDefaultModels() {
        var models = new ArrayList<LLModel>();
        OllamaModels.builder().baseUrl(defaultUrl).build().availableModels().content().forEach(
                model -> models.add(new LLModel(getTitle() + " " + model.getName(), getName(), false, getDefaultUrl(), model.getName(), defaultApiKey))
        );
        return models;
    }

    @Override
    public Tokenizer createTokenizer(LLModel model) {
        return new HuggingFaceTokenizer();
    }

}
