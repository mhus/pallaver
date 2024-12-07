package de.mhus.pallaver.lltype;


import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.LLType;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
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
}
