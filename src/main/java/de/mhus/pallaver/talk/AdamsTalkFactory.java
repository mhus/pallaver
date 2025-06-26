package de.mhus.pallaver.talk;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.mhus.commons.tools.MString;
import de.mhus.pallaver.chat.BubbleFactory;
import de.mhus.pallaver.chat.ChatOptions;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelControl;
import de.mhus.pallaver.model.ModelService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdamsTalkFactory implements TalkControlFactory {

    @Autowired
    private ModelService modelService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String getTitle() {
        return "Duglas Adams";
    }

    @Override
    public ModelControl createModelControl(LLModel model, BubbleFactory bubbleFactory) {
        return new SimpleTalkControl(model, modelService, bubbleFactory);
    }

    @Override
    public String getDefaultPrompt() {
        return """
                Lustige Geschichte mit einem Hasen und einem Roboter.
               """;
    }

    private class SimpleTalkControl implements ModelControl {

        private final LLModel model;
        private final ModelService modelService;
        private final BubbleFactory bubbleFactory;

        public SimpleTalkControl(LLModel model, ModelService modelService, BubbleFactory bubbleFactory) {
            this.model = model;
            this.modelService = modelService;
            this.bubbleFactory = bubbleFactory;
            reset(null);
        }

        @Override
        public void reset(ChatOptions options) {

        }

        @Override
        public AiMessage answer(String userMessage) {
            try {
                var kafka = new SingleTalkControl(model, modelService, bubbleFactory);

                var plotAnswer = kafka.answer(
                        "Plot",
                        """
                                Du bist der Schriftsteller Duglas Adams. Deine Texte sind voller Humor und Ironie.
                                
                                Schreibe die Handlung für ein Buch über:
                                %s
                                
                                Antworte im json Format:
                                {
                                    "title": "Titel der Geschichte",
                                    "characters": {
                                        "name1": "Beschreibung des Charakters",
                                        "name2": "Beschreibung des Charakters"
                                    },
                                    "plot": "Kurze Beschreibung der Handlung"
                                    "chapters": [
                                        {
                                            "title": "Titel des Kapitels",
                                            "description": "Kurze Beschreibung des Kapitels"
                                        }
                                    ]
                                }
                                """.formatted(userMessage));

                var plotJson = extractJson(plotAnswer.text());
                var plot = objectMapper.readValue(plotJson, Plot.class);

                kafka.reset(null);
                kafka.initModel();

                kafka.getChatMemory().add(UserMessage.userMessage(
                        """
                             Du bist der Schriftsteller Duglas Adams. Deine Texte sind voller Humor und Ironie.
                             
                             die Folgende Handlung hast du geschrieben:
                             
                             %s
                             
                             Antworte im json Format:
                             {
                                 "text": "Der Text des Kapitels"
                             }
                             """.formatted(plotJson)
                ));

                for (var chapter : plot.chapters) {
                    kafka.answer(
                            "Kapitel: " + chapter.getTitle(),
                            """
                                    Schreibe das Kapitel '%s'.
                                    
                                    %s
                                    """.formatted(chapter.getTitle(), chapter.getDescription()));
                }

                return plotAnswer;
            } catch (Exception e) {
                bubbleFactory.createBubble("Error").appendText("Error processing Kafka talk: " + e.getMessage());
                throw new RuntimeException("Error processing Kafka talk", e);
            }
        }

        private String extractJson(String text) {
            return MString.beforeIndex(MString.afterIndex(text, "```json"), "```").trim();
        }

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    private static class Plot {
        private String title;
        private Map<String, String> characters;
        private String plot;
        private List<Chapter> chapters;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    private static class Chapter {
        private String title;
        private String description;

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }
    }
}