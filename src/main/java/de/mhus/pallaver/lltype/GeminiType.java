package de.mhus.pallaver.lltype;

import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelOptions;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiTokenCountEstimator;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GeminiType implements LLType {

    @Getter
    private final String name = "gemini";
    @Value("${pallaver.gemini.apiKey:}")
    private String defaultApiKey;
    @Getter
    private final String title = "Gemini";
    private final List<String> supportedFeatures = List.of(LLMFeatures.STREAM, LLMFeatures.TOOLS, LLMFeatures.STREAM_TOOLS);

    private final String[] SUPPORTED_MODELS = {
            "gemini-1.5",
            "gemini-1.5-flash",
            "gemini-1.5-chat",
            "gemini-1.5-pro",
            "gemini-2.0",
            "gemini-2.0-flash",
            "gemini-2.0-chat",
            "gemini-2.0-pro",
            "gemini-2.5",
            "gemini-2.5-flash",
            "gemini-2.5-chat",
            "gemini-2.5-pro"
    };

    @Override
    public List<LLModel> getDefaultModels() {
        var models = new ArrayList<LLModel>();
        Arrays.stream(SUPPORTED_MODELS).forEach(model -> models.add(new LLModel(getTitle() + " " + model, getName(), false, "", model, defaultApiKey)));
        return models;
    }

    @Override
    public TokenCountEstimator createTokenizer(LLModel model) {
        return GoogleAiGeminiTokenCountEstimator
                .builder()
                .apiKey(model.getApiKey())
                .modelName(model.getModel())
                .logRequestsAndResponses(true)
                .build();
    }

    @Override
    public StreamingChatModel createStreamingChatModel(LLModel model, ModelOptions options) {
            return GoogleAiGeminiStreamingChatModel
                    .builder()
                    .apiKey(model.getApiKey())
                    .modelName(model.getModel())
                    .temperature(options.getTemperature())
                    //.topP(options.getTopP())
                    //.maxTokens(options.getMaxTokens())
                    //.frequencyPenalty(options.getFrequencyPenalty())
                    //.presencePenalty(options.getPresencePenalty())
                    .build();
    }

    @Override
    public boolean supports(LLModel model, String feature) {
        return supportedFeatures.contains(feature);
    }

    @Override
    public ChatModel createChatModel(LLModel model, ModelOptions options) {
//        if (streaming) {
//            return new XChatModel(GoogleAiGeminiStreamingChatModel
//                    .builder()
//                    .apiKey(model.getApiKey())
//                    .modelName(model.getModel())
//                    .temperature(options.getTemperature())
//                    //.topP(options.getTopP())
//                    //.maxTokens(options.getMaxTokens())
//                    //.frequencyPenalty(options.getFrequencyPenalty())
//                    //.presencePenalty(options.getPresencePenalty())
//                    .build());
//        }
        return GoogleAiGeminiChatModel
                .builder()
                .apiKey(model.getApiKey())
                .modelName(model.getModel())
                .temperature(options.getTemperature())
                //.topP(options.getTopP())
                //.maxTokens(options.getMaxTokens())
                //.frequencyPenalty(options.getFrequencyPenalty())
                //.presencePenalty(options.getPresencePenalty())
                .build();
    }
}
