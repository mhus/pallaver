package de.mhus.pallaver.lltype;


import de.mhus.commons.tools.MString;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelOptions;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenCountEstimator;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaModels;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

//@Service
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
    public boolean supports(LLModel model, String feature) {
        return supportedFeatures.contains(feature);
    }

    @Override
    public XChatModel createChatModel(LLModel model, ModelOptions options, boolean streaming) {
        if (streaming) {
            var builder = OllamaChatModel.builder();
            builder.baseUrl(model.getUrl());
            builder.modelName(model.getModel());
            builder.temperature(options.getTemperature());
            if (options.getSeed() != null)
                builder.seed(options.getSeed());
            if (options.getTimeoutInSeconds() != null)
                builder.timeout(Duration.ofSeconds(options.getTimeoutInSeconds()));

            if (options.isLogging())
                builder.logRequests(true).logResponses(true);

            return new XChatModel(builder.build());
        }
        var builder = OllamaChatModel.builder();
        builder.baseUrl(model.getUrl());
        builder.modelName(model.getModel());
        builder.temperature(options.getTemperature());
        if (options.getSeed() != null)
            builder.seed(options.getSeed());
        if (options.getTimeoutInSeconds() != null)
            builder.timeout(Duration.ofSeconds(options.getTimeoutInSeconds()));

        if (options.isLogging())
            builder.logRequests(true).logResponses(true);

        return new XChatModel(builder.build());
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
    public TokenCountEstimator createTokenCountEstimator(LLModel model) {
        return new HuggingFaceTokenCountEstimator();
    }

    public static void main(String[] args) {
        var url = "http://localhost:11434";
        var runningModels = OllamaModels.builder().baseUrl(url).build().runningModels();
        System.out.println(runningModels);

        var availableModels = OllamaModels.builder().baseUrl(url).build().availableModels();
        System.out.println(availableModels);

        availableModels.content().forEach(
                model -> {
                    System.out.println("---");
                    System.out.println("Model: " + model.getName());
                    var card = OllamaModels.builder().baseUrl(url).build().modelCard(model.getName()).content();
                    //System.out.println("License: " + card.getLicense());
                    //System.out.println("Modelfile: " + card.getModelfile());
                    System.out.println("Parameters: " + card.getParameters());
                    System.out.println("Modified At: " + card.getModifiedAt());
                    System.out.println("Template: " + card.getTemplate());
                    var details = card.getDetails();
                    if (details != null) {
                        System.out.println("getFormat: " + details.getFormat());
                        System.out.println("getFamily: " + details.getFamily());
                        System.out.println("getFamilies: " + details.getFamilies());
                        System.out.println("getParameterSize: " + details.getParameterSize());
                        System.out.println("getQuantizationLevel: " + details.getQuantizationLevel());
                    }
                }
        );

    }
    public void x() {
    }

}
