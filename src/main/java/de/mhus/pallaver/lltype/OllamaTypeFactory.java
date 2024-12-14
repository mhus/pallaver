package de.mhus.pallaver.lltype;

import de.mhus.pallaver.model.LLType;
import dev.langchain4j.model.ollama.OllamaModels;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class OllamaTypeFactory implements LLTypeFactory {

    @Value("${pallaver.ollama.url:http://localhost:11434}")
    private String url;

    private List<LLType> typeList;

    @PostConstruct
    public void init() {
        typeList = new ArrayList<>();
        OllamaModels.builder().baseUrl(url).build().availableModels().content().forEach(
                model -> typeList.add(new OllamaType(model.getName(), url))
        );
    }

    @Override
    public List<LLType> getTypes() {
        return Collections.unmodifiableList(typeList);
    }
}
