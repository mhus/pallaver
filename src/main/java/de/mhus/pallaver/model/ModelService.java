package de.mhus.pallaver.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mhus.pallaver.ui.LLTypeFactory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
@Slf4j
public class ModelService {

    @Autowired(required = false)
    private List<LLTypeFactory> llTypeFactories;

    @Value("${pallaver.models:models.json}")
    private String modelsFile;

    final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public Collection<LLType> getModelTypes() {
        if (llTypeFactories == null) return Collections.emptyList();
        return llTypeFactories.stream()
                .collect(ArrayList::new,
                        (list, factory) -> list.addAll(factory.getTypes()),
                        ArrayList::addAll);
    }

    public Collection<LLModel> getModels() {
        Set<LLModel> models = new TreeSet<>(Comparator.comparing(LLModel::getTitle));
        try (var inputStream = new FileInputStream(modelsFile)) {
            models.addAll(Arrays.asList(mapper.createParser(inputStream)
                    .readValuesAs(LLModel[].class).next()));
            return Collections.unmodifiableSet(models);
        } catch (Exception e) {
            LOGGER.warn("Can't read models", e);
            return Collections.emptyList();
        }
    }

    public void setModels(Collection<LLModel> models) {
        try (var outputStream = new FileOutputStream(modelsFile)) {
            mapper.writeValue(outputStream, models);
        } catch (Exception e) {
            LOGGER.warn("Can't write models", e);
        }
    }

    public ChatLanguageModel createChatModel(LLModel model) {
        var type = getModelType(model);
        return type.createChatModel(model);
    }

    private LLType getModelType(LLModel model) {
        return getModelTypes().stream().filter(type -> type.getName().equals(model.getType()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown type: " + model.getType()));
    }

    public Tokenizer createTokenizer(LLModel model) {
        var type = getModelType(model);
        return type.createTekenizer(model);
    }

    public StreamingChatLanguageModel createStreamingChatModel(LLModel model) {
        var type = getModelType(model);
        return type.createStreamingChatModel(model);
    }
}
