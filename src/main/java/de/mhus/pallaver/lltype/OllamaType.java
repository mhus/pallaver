package de.mhus.pallaver.lltype;


import de.mhus.pallaver.model.LLType;
import lombok.Getter;

@Getter
public class OllamaType implements LLType {
    private final String name;
    private final String url;
    private final String title;

    public OllamaType(String modelName, String url) {
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
}
