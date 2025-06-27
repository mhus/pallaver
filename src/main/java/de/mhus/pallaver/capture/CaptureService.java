package de.mhus.pallaver.capture;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mhus.commons.tools.MDate;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;

@Service
@Slf4j
public class CaptureService {

    @Autowired
    private ObjectMapper objectMapper;
    private JsonGenerator generator;
    private FileOutputStream fileStream;

    public void capture(String type, String text) {
        init();
        try {
            generator.writeObject(new CaptureEntry(type, text));
            generator.flush();
        } catch (Exception e) {
            LOGGER.error("Capture failed", e);
        }
    }

    private synchronized void init() {
        if (generator != null) return;
        try {
            fileStream = new FileOutputStream("captues/%s.json".formatted(MDate.toIso8601(System.currentTimeMillis())));
            generator = objectMapper.createGenerator(fileStream)
                    .useDefaultPrettyPrinter();
            generator.writeStartArray();
        } catch (Exception e) {
            LOGGER.error("Capture failed", e);
        }
    }

    @PreDestroy
    public void close() {
        if (generator == null) return;
        try {
            generator.writeEndArray();
            generator.close();
            fileStream.close();
        } catch (Exception e) {
            LOGGER.error("Capture close failed", e);
        }
        generator = null;
        fileStream = null;
    }

    public void capture(ChatMessage message) {
        if (message instanceof AiMessage aiMessage) {
            capture("AiMessage", aiMessage.text());
        } else if (message instanceof UserMessage userMessage) {
            capture("UserMessage", userMessage.singleText());
        } else if (message instanceof SystemMessage systemMessage) {
            capture("SystemMessage", systemMessage.text());
        } else {
            capture(message.type().name(), message.toString());
        }
    }
}
