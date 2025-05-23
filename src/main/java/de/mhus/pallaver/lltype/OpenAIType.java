package de.mhus.pallaver.lltype;

import de.mhus.commons.tools.MString;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelOptions;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
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
    private final List<String> supportedFeatures = List.of(LLMFeatures.STREAM, LLMFeatures.TOOLS, LLMFeatures.STREAM_TOOLS);

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
    public TokenCountEstimator createTokenCountEstimator(LLModel model) {
        return new OpenAiTokenCountEstimator(model.getModel());
    }

    @Override
    public boolean supports(LLModel model, String feature) {
        return supportedFeatures.contains(feature);
    }

    @Override
    public XChatModel createChatModel(LLModel model, ModelOptions options, boolean streaming) {
        if (streaming) {
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

            return new XChatModel(builder.build());
        } else {
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

            return new XChatModel(builder.build());
        }
    }
}
