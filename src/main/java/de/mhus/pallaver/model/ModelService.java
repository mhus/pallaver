package de.mhus.pallaver.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mhus.commons.tools.MFile;
import de.mhus.pallaver.lltype.LLType;
import de.mhus.pallaver.lltype.XChatModel;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Service
@Slf4j
public class ModelService {

    @Autowired(required = false)
    private List<LLType> llType;

    @Value("${pallaver.configDirectory:config}")
    private String configDirectory;

    final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public Collection<LLType> getModelTypes() {
        if (llType == null) return Collections.emptyList();
        return Collections.unmodifiableList(llType);
    }

    public Collection<LLModel> getModels() {
        final Set<LLModel> models = new TreeSet<>(Comparator.comparing(LLModel::getTitle));
        Arrays.stream(Objects.requireNonNull(new File(configDirectory + "/models").listFiles(f -> f.getName().endsWith(".json")))).forEach(
                modelFile -> {
                    try (var inputStream = new FileInputStream(modelFile)) {
                        models.add(mapper.createParser(inputStream).readValuesAs(LLModel.class).next() );
                    } catch (Exception e) {
                        LOGGER.warn("Can't read models", e);
                    }
                }
            );

        return Collections.unmodifiableSet(models);
    }

    public void setModels(Collection<LLModel> models) {
        models.forEach(model -> {
            var modelFile = new File(configDirectory + "/models/" + MFile.normalize(model.getTitle()) + ".json");
            try (var outputStream = new FileOutputStream(modelFile)) {
                mapper.writeValue(outputStream, model);
            } catch (Exception e) {
                LOGGER.warn("Can't write models", e);
            }
        });
        Arrays.stream(Objects.requireNonNull(new File(configDirectory + "/models").listFiles(f -> f.getName().endsWith(".json")))).forEach(
                modelFile -> {
                    if (models.stream().noneMatch(m -> modelFile.getName().equals(MFile.normalize(m.getTitle()) + ".json"))) {
                        modelFile.delete();
                    }
                }
        );

    }

    private LLType getModelType(LLModel model) {
        return getModelTypes().stream().filter(type -> type.getName().equals(model.getType()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown type: " + model.getType()));
    }

    public TokenCountEstimator createTokenizer(LLModel model) {
        var type = getModelType(model);
        return type.createTokenCountEstimator(model);
    }

    public boolean supports(LLModel model, String feature) {
        var type = getModelType(model);
        return type.supports(model, feature);
    }

    public XChatModel createChatModel(LLModel model, ModelOptions options, boolean streaming) {
        var type = getModelType(model);
        return type.createChatModel(model, options, streaming);
    }
}
