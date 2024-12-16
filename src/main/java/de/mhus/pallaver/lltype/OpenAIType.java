package de.mhus.pallaver.lltype;

import de.mhus.commons.tools.MString;
import de.mhus.pallaver.LLM;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelOptions;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OpenAIType implements LLType {

    @Getter
    private final String name = "openai";
    @Value("${pallaver.openai.apiKey:}")
    private String defaultApiKey;
    @Getter
    private final String title = "OpenAI";
    private final List<String> supportedFeatures = List.of(LLM.STREAM, LLM.TOOLS);

    @Override
    public String getTitle() {
        return "OpenAI";
    }

    @Override
    public String getName() {
        return "openai";
    }

    @Override
    public List<LLModel> getDefaultModels() {
        var models = new ArrayList<LLModel>();
        Arrays.stream(OpenAiChatModelName.values()).forEach(model -> models.add(new LLModel(getTitle() + " " + model.name(), getName(), false, "", model.toString(), defaultApiKey)));
        return models;
    }

    @Override
    public Tokenizer createTokenizer(LLModel model) {
        return new OpenAiTokenizer(model.getModel());
    }

    @Override
    public StreamingChatLanguageModel createStreamingChatModel(LLModel model, ModelOptions options) {

        var builder = OpenAiStreamingChatModel.builder();
        builder.modelName(model.getModel());
        builder.apiKey(model.getApiKey());
        builder.temperature(options.getTemperature());
        if (MString.isSet(options.getFormat()))
            builder.responseFormat(options.getFormat());
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
        var builder = OpenAiChatModel.builder();
        builder.modelName(model.getModel());
        builder.apiKey(model.getApiKey());
        builder.temperature(options.getTemperature());
        if (MString.isSet(options.getFormat()))
            builder.responseFormat(options.getFormat());
        if (options.getSeed() != null)
            builder.seed(options.getSeed());
        if (options.getTimeoutInSeconds() != null)
            builder.timeout(Duration.ofSeconds(options.getTimeoutInSeconds()));

        if (options.isLogging())
            builder.logRequests(true).logResponses(true);

        return builder.build();
    }
}
